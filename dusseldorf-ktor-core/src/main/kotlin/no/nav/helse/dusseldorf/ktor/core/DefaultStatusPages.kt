package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.call
import io.ktor.features.StatusPages
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.DefaultStatusPages")

private val UNHANDLED_PROBLEM_MESSAGE = DefaultProblemDetails(
        title = "unhandled-error",
        status = 500,
        detail = "En uhåndtert feil har oppstått."
)

fun StatusPages.Configuration.DefaultStatusPages() {

    exception<Throwblem> { cause ->
        logger.trace("Håndtert feil forekom. ${cause.getProblemDetails().asMap()}")
        call.respondProblemDetails(cause.getProblemDetails())
    }

    exception<Throwable> { cause ->
        if (cause is Problem) {
            logger.trace("Håndtert feil forekom. ${cause.getProblemDetails().asMap()}")
            call.respondProblemDetails(cause.getProblemDetails())
        } else {
            logger.error("Uhåndtert feil", cause)
            call.respondProblemDetails(UNHANDLED_PROBLEM_MESSAGE)
        }
    }
}