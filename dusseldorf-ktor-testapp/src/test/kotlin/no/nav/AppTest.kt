package no.nav

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    internal class AppTest {

    @Test
    @Order(1)
    fun `hente metrics`() {
        withNettyEngine(appPort = 1337) {
            "http://localhost:1337/".let {
                assertEquals(HttpStatusCode.OK, "${it}metrics".httpGet().readTextOrThrow().first)
                assertEquals(HttpStatusCode.OK, "${it}proxied-metrics".httpGet().readTextOrThrow().first)
            }
        }
    }

    @Test
    @Order(2)
    fun `UnsafeBlockingTrampoline test`() {
        withNettyEngine(appPort = 1338) {
            "http://localhost:1338/failing-metrics".let {
                doUntilFailure { it.httpGet().second }
            }
        }
    }

    private suspend fun doUntilFailure(block: suspend () -> Result<HttpResponse>) {
        for (i in 1..20) {
            val result = block()
            if (result.isFailure) return
        }
        throw IllegalStateException("Feilet ikke på 20 forsøk")
    }

    private fun withNettyEngine(appPort: Int, block: suspend () -> Unit) {
        val server = embeddedServer(Netty, applicationEngineEnvironment {
            module { app() }
            connector { port = appPort }
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