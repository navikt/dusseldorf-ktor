package no.nav.helse.dusseldorf.ktor.metrics

import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry

fun MicrometerMetrics.Configuration.init(app: String) {
    registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM)
    timers { call, throwable ->
        val hasHttpStatusCode = call.response.status() != null
        tag("app", app)
        tag("code", if (hasHttpStatusCode) "${call.response.status()!!.value}" else "n/a")
        tag("result",
                when {
                    throwable != null -> "failure"
                    !hasHttpStatusCode -> "failure"
                    call.response.status()!!.isSuccessOrRedirect() -> "success"
                    else -> "failure"
                }
        )
    }
}

private fun HttpStatusCode.isSuccessOrRedirect() = value in (200 until 400)