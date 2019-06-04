package no.nav.helse.dusseldorf.ktor.health

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

class HealthService(
        private val healthChecks : Set<HealthCheck>
) {
    private companion object {
        private val logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.health.HealthService")

    }
    internal suspend fun check() : List<Result> {
        val results = coroutineScope {
            val futures = mutableListOf<Deferred<Result>>()
            healthChecks.forEach { healthCheck ->
                futures.add(async {
                    try {
                        healthCheck.check()
                    } catch (cause: Throwable) {
                        logger.error("Feil ved eksekvering av helsesjekk.", cause)
                        UnHealthy(name = healthCheck.javaClass.simpleName, result = cause.message ?: "Feil ved eksekvering av helsesjekk.")
                    }
                })

            }
            futures.awaitAll()
        }
        results.filter { it is UnHealthy }.forEach {
            logger.error("Failing Health Check: ${it.result()}")
        }
        return results
    }
}