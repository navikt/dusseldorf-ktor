package no.nav.helse.dusseldorf.ktor.metrics

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.util.AttributeKey
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry

fun MicrometerMetrics.Configuration.init(
        app: String,
        collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
) {
    registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM)
    timers { call, throwable ->
        tag("app", app)
        tag("result",
                when {
                    throwable != null -> "failure"
                    call.response.status() == null -> "failure"
                    call.response.status()!!.isSuccessOrRedirect() -> "success"
                    else -> "failure"
                }
        )
        tag("problem_details", call.resolveProblemDetailsTag())
    }
}

private fun ApplicationCall.resolveProblemDetailsTag(): String =
        when (val problemDetailsKey = attributes.allKeys.filter { it.name == "problem-details" }.firstOrNull()) {
            null -> "n/a"
            else -> {
                @Suppress("UNCHECKED_CAST")
                attributes[problemDetailsKey as AttributeKey<String>]
            }
        }

private fun HttpStatusCode.isSuccessOrRedirect() = value in (200 until 400)
