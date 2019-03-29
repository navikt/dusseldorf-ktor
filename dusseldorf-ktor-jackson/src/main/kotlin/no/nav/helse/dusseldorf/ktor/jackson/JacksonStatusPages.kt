package no.nav.helse.dusseldorf.ktor.jackson

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages")

fun StatusPages.Configuration.JacksonStatusPages() {

    exception<JsonMappingException> { cause ->
        val violations= mutableSetOf<Violation>()
        cause.path.filter { it.fieldName != null }.forEach {
            violations.add(
                    Violation(
                            parameterType = ParameterType.ENTITY,
                            parameterName = it.fieldName,
                            reason = "Må være satt.",
                            invalidValue = null

                    )
            )
        }

        val problemDetails = ValidationProblemDetails(violations)
        val message = problemDetails.asMap()

        logger.debug("$message", cause)

        call.respond(
                status = HttpStatusCode.fromValue(problemDetails.status),
                message = message
        )
    }

    exception<JsonProcessingException> { cause ->

        val problemDetails = DefaultProblemDetails(
                title = "invalid-json-entity",
                status = 400,
                detail = "Request entityen inneholder ugyldig JSON."
        )
        val message = problemDetails.asMap()

        logger.debug("$message", cause)

        call.respond(
                status = HttpStatusCode.fromValue(problemDetails.status),
                message = message
        )
    }
}