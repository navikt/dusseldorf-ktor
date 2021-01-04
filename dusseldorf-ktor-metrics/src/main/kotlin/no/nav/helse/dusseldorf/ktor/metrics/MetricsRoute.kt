package no.nav.helse.dusseldorf.ktor.metrics

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.helse.dusseldorf.ktor.core.Paths
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute")

fun Route.MetricsRoute(
        collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry,
        path: String = Paths.DEFAULT_METRICS_PATH) {

    fun ApplicationCall.names() =
        request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()


    get(path) {
        logger.debug("Metrics hentes")
        val names = call.names()
        val metrics = collectorRegistry.filteredMetricFamilySamples(names)

        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, metrics)
        }
    }
}