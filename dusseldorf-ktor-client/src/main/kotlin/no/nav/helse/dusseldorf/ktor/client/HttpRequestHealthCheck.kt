package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import org.slf4j.LoggerFactory
import java.net.URL

class HttpRequestHealthCheck(
        app: String,
        private val urlExpectedHttpStatusCodeMap : Map<URL, HttpStatusCode>
) : HealthCheck {
    private val logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck")

    private val monitoredHttpClient = MonitoredHttpClient(
            source = app,
            destination = "HttpRequestHealthCheck",
            httpClient = HttpClient(Apache) {
                engine {
                    socketTimeout = 2_000  // Max time between TCP packets - default 10 seconds
                    connectTimeout = 2_000 // Max time to establish an HTTP connection - default 10 seconds
                    customizeClient { setProxyRoutePlanner() }
                }
                install (Logging) {
                    sl4jLogger("HttpRequestHealthCheck")
                }
            }
    )


    override suspend fun check(): Result {
        val responses = coroutineScope {
            val futures = mutableListOf<Deferred<Response>>()
            urlExpectedHttpStatusCodeMap.forEach { url, expectedHttpStatusCode ->
                futures.add(async {
                    val httpRequestBuilder = HttpRequestBuilder()
                    httpRequestBuilder.method = HttpMethod.Get
                    httpRequestBuilder.url(url)

                    try {
                        monitoredHttpClient.request(httpRequestBuilder).use {
                            Response(
                                    url = url,
                                    expectedHttpStatusCode = expectedHttpStatusCode,
                                    actualHttpStatusCode = it.status,
                                    message = it.readText()
                            )
                        }
                    } catch (cause: Throwable) {
                        logger.error("Ingen response mot $url", cause)
                        Response(
                                url = url,
                                expectedHttpStatusCode = expectedHttpStatusCode,
                                actualHttpStatusCode = null,
                                message = "Mottok ingen response. ${cause.message}"
                        )
                    }

                })
            }
            futures.awaitAll()
        }


        val result = mutableMapOf<String, Any?>()
        val isHealthy = responses.none { it.isUnhealthy() }

        responses.forEach { response ->
            result[response.url.toString()] = mapOf(
                    Pair("message", response.message),
                    Pair("expected_http_status_code", response.expectedHttpStatusCode.value),
                    Pair("actual_http_status_code", response.actualHttpStatusCode?.value)
            )
        }

        return if (isHealthy) Healthy(name = "HttpRequestHealthCheck", result = result) else UnHealthy(name = "HttpRequestHealthCheck", result = result)
    }
}

private data class Response(
        val url : URL,
        val expectedHttpStatusCode: HttpStatusCode,
        val actualHttpStatusCode: HttpStatusCode?,
        val message: String? = null
) {
    internal fun isUnhealthy() : Boolean = expectedHttpStatusCode != actualHttpStatusCode
}