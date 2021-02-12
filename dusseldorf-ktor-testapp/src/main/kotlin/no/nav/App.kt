package no.nav

import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.httpGet as fuelHttpGet
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.delay
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet as ktorHttpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow

import no.nav.helse.dusseldorf.ktor.core.DefaultProbeRoutes
import no.nav.helse.dusseldorf.ktor.core.FullførAktiveRequester
import no.nav.helse.dusseldorf.ktor.core.PreStopRoute
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("no.nav.App")


@KtorExperimentalAPI
fun Application.app() {
    DefaultExports.initialize()

    val fullførAktiveRequester = FullførAktiveRequester(application = this)

    routing {
        DefaultProbeRoutes()
        MetricsRoute()
        PreStopRoute(
            preStopActions = listOf(fullførAktiveRequester)
        )

        get("/treg-request") {
            logger.info("treg request starter")
            delay(5000)
            logger.info("treg request ferdig")
            call.respond("DONE")
        }

        suspend fun String.proxy() =
            this.ktorHttpGet().readTextOrThrow().second to this.fuelHttpGet().awaitString()

        get("/proxied-metrics") {
            val (isAliveA, isAliveB) = "http://localhost:1337/isalive".proxy()
            require(setOf(isAliveA, isAliveB, "ALIVE").size == 1)
            val (isReadyA, isReadyB) = "http://localhost:1337/isready".proxy()
            require(setOf(isReadyA, isReadyB, "READY").size == 1)
            val (metricsA, metricsB) = "http://localhost:1337/metrics".proxy()
            require(metricsA.contains("# HELP") && metricsB.contains("# HELP"))
            call.respond(metricsB)
        }
    }
}
