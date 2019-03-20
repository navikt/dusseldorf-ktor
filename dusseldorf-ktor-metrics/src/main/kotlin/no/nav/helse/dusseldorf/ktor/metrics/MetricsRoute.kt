package no.nav.helse.dusseldorf.ktor.metrics

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute")

fun Route.MetricsRoute(
        collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry,
        path: String = "/metrics"
) {
    get(path) {
        logger.debug("Metrics hentes")
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: Collections.emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}