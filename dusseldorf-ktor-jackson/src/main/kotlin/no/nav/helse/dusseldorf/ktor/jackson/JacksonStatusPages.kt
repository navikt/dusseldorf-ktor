package no.nav.helse.dusseldorf.ktor.jackson

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.path
import io.ktor.util.InternalAPI
import io.ktor.util.rootCause
import no.nav.helse.dusseldorf.ktor.core.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages")

@OptIn(InternalAPI::class)
fun StatusPagesConfig.JacksonStatusPages() {

    exception<JsonMappingException> { call, cause ->
        if (cause.cause is IllegalArgumentException) {
            call.respondProblemDetails(
                DefaultProblemDetails(
                    title = "IllegalArgumentException",
                    status = 400,
                    detail = "${cause.cause as IllegalArgumentException} -> ${cause.path}"
                ),
                logger
            )
        } else {
            val violations = mutableSetOf<Violation>()
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
    }

    exception<JsonProcessingException> { call, cause ->

        val problemDetails = DefaultProblemDetails(
                title = "invalid-json-entity",
                status = 400,
                detail = "Request entityen inneholder ugyldig JSON."
        )
        logger.debug("Feil ved prosessering av JSON", cause)
        call.respondProblemDetails(problemDetails, logger)
    }

    exception { call: ApplicationCall, cause: BadRequestException ->
        val problemDetails = when (val rootCause = cause.rootCause) {
            is MissingKotlinParameterException -> {
                val parameter = rootCause.parameter
                ValidationProblemDetails(
                    setOf(
                        Violation(
                            parameterName = parameter.name ?: "ukjent",
                            parameterType = ParameterType.ENTITY,
                            reason = "Må være satt.",
                            invalidValue = null
                        )
                    )
                )
            }
            else -> {
                DefaultProblemDetails(
                    title = "invalid-request-parameters",
                    status = 400,
                    detail = "Requesten inneholder ugyldige parametere.",
                    instance = URI(call.request.path())
                )
            }
        }

        call.respondProblemDetails(problemDetails, logger)
    }
}
