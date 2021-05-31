package no.nav

import io.ktor.application.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.delay
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import no.nav.helse.dusseldorf.ktor.core.DefaultProbeRoutes
import no.nav.helse.dusseldorf.ktor.core.FullførAktiveRequester
import no.nav.helse.dusseldorf.ktor.core.PreStopRoute
import no.nav.helse.dusseldorf.ktor.core.preStopOnApplicationStopPreparing
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("no.nav.App")

@KtorExperimentalAPI
fun Application.app() {
    DefaultExports.initialize()

    val preStopActions = listOf(FullførAktiveRequester(application = this))
    preStopOnApplicationStopPreparing(preStopActions)

    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        DefaultProbeRoutes()
        MetricsRoute()
        PreStopRoute(preStopActions)
        HealthRoute(
            healthService = HealthService(setOf(
                HttpRequestHealthCheck(mapOf(
                    URI("http://localhost:1337/isalive") to HttpRequestHealthConfig(
                        expectedStatus = HttpStatusCode.OK,
                        includeExpectedStatusEntity = true
                    ),
                    URI("http://localhost:1337/isready") to HttpRequestHealthConfig(
                        expectedStatus = HttpStatusCode.OK,
                        includeExpectedStatusEntity = true
                    ),
                    URI("http://localhost:1337/not-found") to HttpRequestHealthConfig(
                        expectedStatus = HttpStatusCode.NotFound,
                        includeExpectedStatusEntity = true
                    )
                ))
            ))
        )

        get("/treg-request") {
            logger.info("treg request starter")
            delay(5000)
            logger.info("treg request ferdig")
            call.respond("DONE")
        }

        suspend fun String.proxy() =
            this.httpGet().readTextOrThrow().second

        get("/proxied-metrics") {
            val isAlive = "http://localhost:1337/isalive".proxy()
            require("ALIVE" == isAlive)
            val isReady = "http://localhost:1337/isready".proxy()
            require("READY" == isReady)
            val metrics = "http://localhost:1337/metrics".proxy()
            require(metrics.contains("# HELP"))
            val health = "http://localhost:1337/health".httpGet().second.getOrThrow()
            require(health.status == HttpStatusCode.OK)
            health.readText().also { require(it.contains("ALIVE") && it.contains("READY")) }
            call.respond(metrics)
        }
    }
}
