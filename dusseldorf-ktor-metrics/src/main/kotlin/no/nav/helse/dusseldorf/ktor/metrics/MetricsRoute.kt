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
import java.io.CharArrayWriter

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute")
private val metricsContentType = ContentType.parse(TextFormat.CONTENT_TYPE_004)

fun Route.MetricsRoute(
        collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry,
        path: String = Paths.DEFAULT_METRICS_PATH) {

    fun ApplicationCall.names() =
        request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()


    get(path) {
        logger.debug("Metrics hentes")
        val names = call.names()
        val metrics = collectorRegistry.filteredMetricFamilySamples(names)
        val formatted = CharArrayWriter(1024)
            .also { TextFormat.write004(it, metrics) }
            .use { it.toString() }

        call.respondText(
            status = HttpStatusCode.OK,
            contentType = metricsContentType,
            text = formatted
        )
    }
}