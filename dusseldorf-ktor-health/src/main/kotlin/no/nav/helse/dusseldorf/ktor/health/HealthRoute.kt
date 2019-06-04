package no.nav.helse.dusseldorf.ktor.health

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.helse.dusseldorf.ktor.core.Paths
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.system.measureTimeMillis

fun Route.HealthRoute(
        path: String = Paths.DEFAULT_HEALTH_PATH,
        healthService: HealthService
) {

    get(path) {
        val healthy : MutableList<Map<String, Any?>> = mutableListOf()
        val unhealthy : MutableList<Map<String, Any?>> = mutableListOf()


        val duration = Duration.of(measureTimeMillis {
            val results = healthService.check()
            results.forEach { result ->
                when (result) {
                    is Healthy -> healthy.add(result.result())
                    else -> unhealthy.add(result.result())
                }
            }
        }, ChronoUnit.MILLIS)

        call.respond(
                status = if (unhealthy.isEmpty()) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
                message = mapOf(
                    "duration" to duration.toString(), // ISO-8601
                    "healthy" to healthy,
                    "unhealthy" to unhealthy
                )
        )
    }
}