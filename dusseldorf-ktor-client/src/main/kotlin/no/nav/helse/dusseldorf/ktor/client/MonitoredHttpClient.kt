package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.call.receive
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.prometheus.client.Counter
import io.prometheus.client.Histogram

class MonitoredHttpClient (
        val source: String,
        val destination: String,
        val httpClient: HttpClient,
        val overridePaths : Map<Regex, String> = mapOf()
) {

    val histogram = Histogram
            .build("sent_http_requests_histogram",
                    "Histogram for alle HTTP-requester sendt fra $source")
            .labelNames("source", "destination", "verb", "path")
            .register()

    val counter = Counter
            .build(
                    "sent_http_requests_counter",
                    "Teller for alle HTTP-requester som sendes fra $source")
            .labelNames("source", "destination", "verb", "path", "status")
            .register()


    suspend inline fun request(
            httpRequestBuilder: HttpRequestBuilder,
            expectedHttpResponseCodes : Set<HttpStatusCode> = setOf(HttpStatusCode.OK)
    ) : HttpResponse {
        val path = getPath(httpRequestBuilder.url, overridePaths)
        val verb = httpRequestBuilder.method.value

        val timer = histogram.labels(source, destination, verb, path).startTimer()

        val response : HttpResponse = try {
            httpClient.call(httpRequestBuilder).response
        } catch (cause: Throwable) {
            counter.labels(source, destination, verb, path, "network_error").inc()
            throw IllegalStateException("network_error", cause)
        } finally {
            timer.observeDuration()
        }
        if (!expectedHttpResponseCodes.contains(response.status)) {
            counter.labels(source, destination, verb, path, "unexpected_http_response_code").inc()
            throw IllegalStateException("unexpected_http_response_code")
        }
        return response
    }

    suspend inline fun <reified T> requestAndReceive(
            httpRequestBuilder: HttpRequestBuilder,
            expectedHttpResponseCodes : Set<HttpStatusCode> = setOf(HttpStatusCode.OK)
    ) : T {
        val path = getPath(httpRequestBuilder.url, overridePaths)
        val verb = httpRequestBuilder.method.value

        val response = request(
                httpRequestBuilder = httpRequestBuilder,
                expectedHttpResponseCodes = expectedHttpResponseCodes
        )

        return response.use {
            try {
                val result = it.receive<T>()
                counter.labels(source, destination, verb, path, "success").inc()
                result
            } catch (cause: Throwable) {
                counter.labels(source, destination, verb, path, "response_mapping_error").inc()
                throw IllegalStateException("response_mapping_error")
            }
        }
    }
}

fun getPath(
        urlBuilder: URLBuilder,
        overridePaths: Map<Regex, String>
) : String {
    val path = urlBuilder.encodedPath
    overridePaths.forEach {
        if (path.matches(it.key)) {
            return it.value
        }
    }
    return path
}