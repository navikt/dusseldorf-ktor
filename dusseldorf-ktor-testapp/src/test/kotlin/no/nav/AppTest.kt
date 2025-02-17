package no.nav

import io.ktor.client.plugins.*
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

internal class AppTest {

    @Test
    fun `hente metrics & pre-stop`() {
        withNettyEngine(appPort = 1337) {
            "http://localhost:1337/".let {
                doNTimes(n=2) { GlobalScope.launch(Dispatchers.IO) { "${it}treg-request".httpGet{ builder ->
                    builder.timeout { requestTimeoutMillis = 10000 }
                }.second.getOrThrow() } }
                doNTimes {
                    assertEquals(HttpStatusCode.OK, "${it}metrics".httpGet().readTextOrThrow().first)
                    assertEquals(HttpStatusCode.OK, "${it}proxied-metrics".httpGet().readTextOrThrow().first)
                }
                assertEquals(HttpStatusCode.OK, "${it}internal/pre-stop".httpGet { builder ->
                    builder.timeout { requestTimeoutMillis = 10000 }
                }.readTextOrThrow().first)
            }
        }
    }

    @Test
    fun `Tester endepunkt som krever callId`() {
        withNettyEngine(appPort = 1337) {
            val getResponse: Pair<HttpRequestData, Result<HttpResponse>> = "http://localhost:1337/requires-call-id".httpGet {
                it.headers {
                    append(HttpHeaders.XCorrelationId, "2e95bcc4-5d75-4a56-9633-a42c4b05a734")
                }
            }

            val responseText = getResponse.readTextOrThrow()
            assertEquals(HttpStatusCode.OK, responseText.first, responseText.second)
        }
    }

    @Test
    fun `Tester health og metrics endepunkter ikke trenger callId`() {
        withNettyEngine(appPort = 1337) {
            listOf("/isready", "/isalive", "/metrics", "/health").forEach {
                val getResponse: Pair<HttpRequestData, Result<HttpResponse>> = "http://localhost:1337$it".httpGet()

                val responseText = getResponse.readTextOrThrow()
                assertEquals(HttpStatusCode.OK, responseText.first, responseText.second)
            }
        }
    }

    private suspend fun doNTimes(n: Int = 20, block: suspend () -> Any) {
        for (i in 1..n) { block() }
    }

    private fun withNettyEngine(appPort: Int, block: suspend () -> Unit) {
        val server = embeddedServer(
            factory = Netty,
            environment = applicationEnvironment {},
            configure = {
                connector { port = appPort }
            },
            module = {
                app()
            }
        )
        val job = GlobalScope.launch {
            server.start(wait = true)
        }

        runBlocking {
            for (i in 1..20) {
                delay(i * 1000L)
                if ("http://localhost:$appPort/isready".httpGet().second.isSuccess) {
                    break
                }
            }
        }

        try {
            runBlocking { block() }
        } finally {
            server.stop(1000,1000)
            runBlocking { job.cancelAndJoin() }
        }
    }
}
