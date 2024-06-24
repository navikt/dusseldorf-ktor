package no.nav.helse.dusseldorf.ktor.metrics

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.helse.dusseldorf.ktor.core.Paths
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute")

fun Route.MetricsRoute(
    registry : PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, PrometheusRegistry.defaultRegistry, Clock.SYSTEM),
    path: String = Paths.DEFAULT_METRICS_PATH
) {

    get(path) {
        logger.debug("Metrics hentes")
        call.respondText { registry.scrape() }
    }
}
