package no.nav.helse.dusseldorf.ktor.core

class Paths {
    companion object {
        const val DEFAULT_METRICS_PATH = "/metrics"
        const val DEFAULT_READY_PATH = "/isready"
        const val DEFAULT_HEALTH_PATH = "/health"
        const val DEFAULT_ALIVE_PATH = "/isalive"
        val DEFAULT_EXCLUDED_PATHS = setOf(DEFAULT_METRICS_PATH, DEFAULT_READY_PATH, DEFAULT_ALIVE_PATH, DEFAULT_HEALTH_PATH, "/favicon.ico")
    }
}