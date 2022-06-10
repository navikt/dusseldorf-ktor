package no.nav.helse.dusseldorf.ktor.core

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.DefaultProbeRouts")

fun Route.DefaultProbeRoutes(
        readyPath : String = Paths.DEFAULT_READY_PATH,
        alivePath : String = Paths.DEFAULT_ALIVE_PATH
) {
    get(alivePath) {
        logger.debug("alive")
        call.respondText("ALIVE")
    }

    get(readyPath) {
        logger.debug("ready")
        call.respondText("READY")
    }
}
