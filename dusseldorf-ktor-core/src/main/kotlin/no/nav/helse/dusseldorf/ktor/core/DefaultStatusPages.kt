package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.response.header
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
        call.response.header("invalid-parameters", cause.getProblemDetails().invalidParametersSomString())
        call.respondProblemDetails(cause.getProblemDetails(), logger)
    }

    exception<Throwable> { cause ->
        if (cause is Problem) {
            call.response.header("invalid-parameters", cause.getProblemDetails().invalidParametersSomString())
            call.respondProblemDetails(cause.getProblemDetails(), logger)
        } else {
            logger.error("Uhåndtert feil", cause)
            call.respondProblemDetails(UNHANDLED_PROBLEM_MESSAGE, logger)
        }
    }
}

private fun ProblemDetails.invalidParametersSomString(): String = asMap()["invalid_parameters"].toString()
