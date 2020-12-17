package no.nav

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AppTest {

    @Test
    fun `hente metrics`() {
        withNettyEngine {
            val responseCode = "http://localhost:8888/metrics".httpGet().readTextOrThrow().first
            assertEquals(HttpStatusCode.OK, responseCode)
        }
    }

    private fun withNettyEngine(block: suspend () -> Unit) {
        val server = embeddedServer(Netty, applicationEngineEnvironment {
            module { app() }
            connector { port = 8888 }
        })
        val job = GlobalScope.launch {
            server.start(wait = true)
        }
        try {
            runBlocking { block() }
        } finally {
            server.stop(1000,1000)
            runBlocking { job.cancelAndJoin() }
        }
    }
}