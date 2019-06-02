package no.nav.helse.dusseldorf.ktor.metrics

import io.ktor.http.isSuccess
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry

fun MicrometerMetrics.Configuration.init(app: String) {
    registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM)
    timers { call, throwable ->
        tag("app", app)
        tag("status",
                when {
                    throwable != null -> "failure"
                    call.response.status() == null -> "failure"
                    call.response.status()!!.isSuccess() -> "success"
                    else -> "failure"
                }
        )
    }
}