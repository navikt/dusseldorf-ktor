package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.DefaultStatusPages")


private val UNHANDLED_HTTP_STATUS_CODE = HttpStatusCode.InternalServerError
private val UNHANDLED_PROBLEM_DETAILS = DefaultProblemDetails(
        title = "unhandled-error",
        status = UNHANDLED_HTTP_STATUS_CODE.value,
        details = "En uhåndtert feil har oppstått."
)
fun StatusPages.Configuration.DefaultStatusPages() {

    exception<Throwblem> { cause ->
        logger.trace("Håndtert feil forekom. ${cause.getProblemDetails().asMap()}")
        call.respond(
                status = HttpStatusCode.fromValue(cause.getProblemDetails().status),
                message = cause.getProblemDetails().asMap()
        )
    }

    exception<Throwable> { cause ->
        if (cause is Problem) {
            logger.trace("Håndtert feil forekom. ${cause.getProblemDetails().asMap()}")
            call.respond(
                    status = HttpStatusCode.fromValue(cause.getProblemDetails().status),
                    message = cause.getProblemDetails().asMap()
            )
        } else {
            logger.error("Uhåndtert feil", cause)
            call.respond(
                    status = UNHANDLED_HTTP_STATUS_CODE,
                    message = UNHANDLED_PROBLEM_DETAILS.asMap()
            )
        }
    }
}