package no.nav.helse.dusseldorf.ktor.jackson

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.response.header
import no.nav.helse.dusseldorf.ktor.core.*
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

        logger.debug("Feil ved mapping av JSON", cause)
        call.respondProblemDetails(problemDetails, logger)
    }

    exception<JsonProcessingException> { cause ->

        val problemDetails = DefaultProblemDetails(
                title = "invalid-json-entity",
                status = 400,
                detail = "Request entityen inneholder ugyldig JSON."
        )
        logger.debug("Feil ved prosessering av JSON", cause)
        call.respondProblemDetails(problemDetails, logger)
    }
}
