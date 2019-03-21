package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.response.header
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
private val JSON_UTF_8 = ContentType.Application.Json.withCharset(Charsets.UTF_8)

fun StatusPages.Configuration.DefaultStatusPages() {

    exception<Throwblem> { cause ->
        call.setContentType()

        logger.trace("Håndtert feil forekom. ${cause.getProblemDetails().asMap()}")
        call.respond(
                status = HttpStatusCode.fromValue(cause.getProblemDetails().status),
                message = cause.getProblemDetails()
        )
    }

    exception<Throwable> { cause ->
        call.setContentType()

        if (cause is Problem) {
            logger.trace("Håndtert feil forekom. ${cause.getProblemDetails().asMap()}")
            call.respond(
                    status = HttpStatusCode.fromValue(cause.getProblemDetails().status),
                    message = cause.getProblemDetails()
            )
        } else {
            logger.error("Uhåndtert feil", cause)
            call.respond(
                    status = UNHANDLED_HTTP_STATUS_CODE,
                    message = UNHANDLED_PROBLEM_DETAILS
            )
        }
    }
}

private fun ApplicationCall.setContentType() {
    response.header(HttpHeaders.ContentType, JSON_UTF_8.toString())
}
