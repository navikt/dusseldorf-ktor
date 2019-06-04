package no.nav.helse.dusseldorf.ktor.health

import io.prometheus.client.Gauge
import kotlinx.coroutines.runBlocking
import java.time.Duration
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

class HealthReporter(
        private val healthService: HealthService
) {
    init {
        Runtime.getRuntime().addShutdownHook(thread {
            timer.cancel()
        })
    }

    private companion object {
        private val gauge = Gauge
                .build("health_check_status",
                        "Indikerer applikasjonens helse status. 1=OK, 0=Feiler")
                .labelNames("app")
                .register()
    }

    private val timer = fixedRateTimer(
            name = "health_reporter",
            initialDelay = Duration.ofMinutes(1).toMillis(),
            period = Duration.ofSeconds(30).toMillis()) {
        val results = runBlocking { healthService.check() }
        gauge.setFromResults(results)
    }
}

private const val HEALTHY = 0.0
private const val UNHEALTHY = 1.0
private fun Gauge.setFromResults(results: List<Result>) {
    if (results.any { it is UnHealthy }) {
        set(UNHEALTHY)
    } else {
        set(HEALTHY)
    }
}