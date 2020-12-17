package no.nav

import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.httpGet as fuelHttpGet
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet as ktorHttpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow

import no.nav.helse.dusseldorf.ktor.core.DefaultProbeRoutes
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
fun Application.app() {
    DefaultExports.initialize()

    routing {
        DefaultProbeRoutes()
        MetricsRoute()

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

        get("/failing-metrics") {
            val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
            }
        }
    }
}
