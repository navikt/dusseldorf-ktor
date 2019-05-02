package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.call.receive
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.response.HttpResponse
import io.ktor.http.*
import io.prometheus.client.Counter
import io.prometheus.client.Histogram

val histogram = Histogram
        .build("sent_http_requests_histogram",
                "Histogram for alle HTTP-requester sendt.")
        .labelNames("source", "destination", "verb", "path")
        .register()

val counter = Counter
        .build(
                "sent_http_requests_counter",
                "Teller for alle HTTP-requester som sendes.")
        .labelNames("source", "destination", "verb", "path", "status")
        .register()

class MonitoredHttpClient (
        val source: String,
        val destination: String,
        val httpClient: HttpClient,
        val overridePaths : Map<Regex, String> = mapOf()
) {
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
            throw SentHttpRequestException(
                    status =  "network_error",
                    httpMethod = httpRequestBuilder.method,
                    url = httpRequestBuilder.url.clone().build(),
                    destination = destination,
                    path = path,
                    throwable = cause
            )
        } finally {
            timer.observeDuration()
        }
        if (expectedHttpResponseCodes.isNotEmpty() && !expectedHttpResponseCodes.contains(response.status)) {
            counter.labels(source, destination, verb, path, "unexpected_http_response_code").inc()
            response.use { }
            throw SentHttpRequestException(
                    status =  "unexpected_http_response_code",
                    httpMethod = httpRequestBuilder.method,
                    url = httpRequestBuilder.url.clone().build(),
                    destination = destination,
                    path = path,
                    httpStatusCode = response.status
            )
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
                counter.labels(source, destination, verb, path, "response_receiving_error").inc()
                throw SentHttpRequestException(
                        status =  "response_receiving_error",
                        httpMethod = httpRequestBuilder.method,
                        url = httpRequestBuilder.url.clone().build(),
                        destination = destination,
                        path = path,
                        throwable = cause
                )
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

class SentHttpRequestException(
        status: String,
        httpMethod : HttpMethod,
        url : Url,
        destination: String,
        path : String,
        httpStatusCode: HttpStatusCode? = null,
        throwable: Throwable? = null
) : RuntimeException (
        "status='$status', httpMethod='$httpMethod', url='$url', destination='$destination', path='$path', httpStatusCode='$httpStatusCode'", throwable
)