package no.nav.helse.dusseldorf.ktor.health

import io.prometheus.client.Gauge
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.util.*
import kotlin.concurrent.fixedRateTimer

class HealthReporter(
        private val app: String,
        private val healthService: HealthService,
        frequency: Duration = Duration.ofSeconds(30)
) {

    private companion object {
        private const val HEALTHY = 0.0
        private const val UNHEALTHY = 1.0

        private val gauge = Gauge
                .build("health_check_status",
                        "Indikerer applikasjonens helse status. 0 er OK, 1 indikerer feil.")
                .labelNames("app")
                .register()
    }

    private val timer: Timer

    init {
        timer = fixedRateTimer(
                name = "health_reporter",
                initialDelay = Duration.ofMinutes(1).toMillis(),
                period = frequency.toMillis()) {
            val results = runBlocking { healthService.check() }
            gauge.setFromResults(results)
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            timer.cancel()
        })
    }

    private fun Gauge.setFromResults(results: List<Result>) {
        if (results.any { it is UnHealthy }) {
            labels(app).set(UNHEALTHY)
        } else {
            labels(app).set(HEALTHY)
        }
    }
}