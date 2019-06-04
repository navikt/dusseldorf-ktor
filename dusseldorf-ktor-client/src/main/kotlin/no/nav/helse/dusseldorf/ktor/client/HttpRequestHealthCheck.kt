package no.nav.helse.dusseldorf.ktor.client

import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import java.time.Duration

data class HttpRequestHealthConfig(
        internal val expectedStatus: HttpStatusCode,
        internal val includeExpectedStatusEntity : Boolean = true
)

class HttpRequestHealthCheck(
        private val urlConfigMap : Map<URI, HttpRequestHealthConfig>
) : HealthCheck {

    private companion object {
        private val timeout = Duration.ofSeconds(2).toMillisPart()
        private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck")
    }

    init {
        urlConfigMap.forEach { uri, config ->
            logger.info("$uri -> ${config.expectedStatus.value}")
        }
    }

    override suspend fun check(): Result {
        val triplets = coroutineScope {
            val futures = mutableListOf<Deferred<ResponseResultOf<String>>>()
            urlConfigMap.forEach { url, _ ->
                futures.add(async {
                    url.toString()
                            .httpGet()
                            .timeout(timeout)
                            .timeoutRead(timeout)
                            .awaitStringResponseResult()

                })
            }
            futures.awaitAll()
        }

        val success = JSONObject()
        val failure = JSONObject()

        triplets.forEach { (request,response,result) ->
            val json = JSONObject()
            val key = request.url.toString()
            val config = urlConfigMap.getValue(request.url.toURI())
            val expected = config.expectedStatus.value
            val actual : Int? = if (response.statusCode == -1) null else response.statusCode
            val isExpected = expected == actual

            val message = result.fold(
                    { success ->
                        if (isExpected) {
                            if (config.includeExpectedStatusEntity) {
                                success.jsonOrString()
                            } else "Healthy!"
                        } else success.jsonOrString()
                    },
                    { error ->
                        if (error.cause != null) {
                            logger.error(error.toString())
                            error.cause!!.message?: error.message?: "Ukjent feil."
                        } else {
                            if (isExpected) {
                                if (config.includeExpectedStatusEntity) {
                                    (String(error.response.data).jsonOrString())
                                } else "Healthy!"
                            } else (String(error.response.data).jsonOrString())
                        }
                    }
            )

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