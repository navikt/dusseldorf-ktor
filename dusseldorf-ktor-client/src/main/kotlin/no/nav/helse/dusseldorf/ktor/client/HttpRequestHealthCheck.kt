package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

data class HttpRequestHealthConfig(
        internal val expectedStatus: HttpStatusCode,
        internal val includeExpectedStatusEntity : Boolean = true,
        internal val httpHeaders : Map<String, String> = emptyMap()
)

class HttpRequestHealthCheck(
        private val urlConfigMap : Map<URI, HttpRequestHealthConfig>
) : HealthCheck {

    private companion object {
        private val timeoutMillis = 2000L
        private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck")
    }

    init {
        urlConfigMap.forEach { uri, config ->
            logger.info("$uri -> ${config.expectedStatus.value}")
        }
    }

    override suspend fun check(): Result {
        val triplets = coroutineScope {
            val futures = mutableListOf<Deferred<Pair<HttpRequestData, kotlin.Result<HttpResponse>>>>()
            urlConfigMap.forEach { (url, config) ->
                futures.add(async {
                    url.httpGet { builder ->
                        config.httpHeaders.forEach { (key, value) ->
                            builder.header(key, value)
                        }
                        builder.timeout {
                            requestTimeoutMillis = timeoutMillis
                            connectTimeoutMillis = timeoutMillis
                            socketTimeoutMillis = timeoutMillis
                        }
                    }
                })
            }
            futures.awaitAll()
        }

        val success = JSONObject()
        val failure = JSONObject()

        triplets.forEach { (httpRequest, httpResponseResult) ->
            val json = JSONObject()
            val requestUri = httpRequest.url.toURI()
            val key = requestUri.toString()
            val config = urlConfigMap.getValue(requestUri)
            val expected = config.expectedStatus.value
            val httpResponse = httpResponseResult.getOrNull()
            val actual = httpResponse?.status?.value
            val isExpected = expected == actual

            val message = when {
                httpResponse == null -> httpResponseResult.exceptionOrNull()?.cause?.message ?: httpResponseResult.exceptionOrNull()?.message ?: "Ukjent feil.".also {
                    logger.error(it)
                }
                !isExpected || (isExpected && config.includeExpectedStatusEntity) -> httpResponse.readText().jsonOrString()
                else -> "Healthy!"
            }

            json.put("message", message)
            json.put("expected_http_status_code", expected)
            json.put("actual_http_status_code", actual)

            if (isExpected) success.put(key, json) else failure.put(key, json)
        }

        val json = JSONObject()
        json.put("success", success)
        json.put("failure", failure)

        return if (failure.isEmpty) Healthy(name = "HttpRequestHealthCheck", result = json.toMap())
        else UnHealthy(name = "HttpRequestHealthCheck", result = json.toMap())
    }
}

private fun String.jsonOrString() : Any {
    return try { JSONObject(this) }
    catch (cause: JSONException) {
        try { JSONArray(this)
        } catch (cause : JSONException) { this }
    }
}