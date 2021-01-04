package no.nav

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AppTest {

    @Test
    fun `hente metrics`() {
        withNettyEngine(appPort = 1337) {
            "http://localhost:1337/".let {
                doNTimes {
                    assertEquals(HttpStatusCode.OK, "${it}metrics".httpGet().readTextOrThrow().first)
                    assertEquals(HttpStatusCode.OK, "${it}proxied-metrics".httpGet().readTextOrThrow().first)
                }
            }
        }
    }

    private suspend fun doNTimes(n: Int = 20,block: suspend () -> Any) {
        for (i in 1..n) { block() }
    }

    private fun withNettyEngine(appPort: Int, block: suspend () -> Unit) {
        val server = embeddedServer(Netty, applicationEngineEnvironment {
            module { app() }
            connector { port = appPort }
        })
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