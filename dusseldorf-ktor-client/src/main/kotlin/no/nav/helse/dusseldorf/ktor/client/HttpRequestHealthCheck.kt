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
import java.net.URL
import java.time.Duration

class HttpRequestHealthCheck(
        private val urlExpectedHttpStatusCodeMap : Map<URL, HttpStatusCode>
) : HealthCheck {

    private companion object {
        private val timeout = Duration.ofSeconds(2).toMillisPart()
        private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck")
    }

    override suspend fun check(): Result {
        val triplets = coroutineScope {
            val futures = mutableListOf<Deferred<ResponseResultOf<String>>>()
            urlExpectedHttpStatusCodeMap.forEach { url, _ ->
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
            val expected = urlExpectedHttpStatusCodeMap.getValue(request.url).value
            val actual : Int? = if (response.statusCode == -1) null else response.statusCode

            val message = result.fold(
                    { success -> success.jsonOrString() },
                    { error ->
                        if (error.cause != null) {
                            logger.error(error.toString())
                            error.cause!!.message?: error.message?: "Ukjent feil."
                        }
                        else { (String(error.response.data).jsonOrString()) }
                    }
            )

            json.put("message", message)
            json.put("expected_http_status_code", expected)
            json.put("actual_http_status_code", actual)

            if (expected == actual) success.put(key, json) else failure.put(key, json)
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