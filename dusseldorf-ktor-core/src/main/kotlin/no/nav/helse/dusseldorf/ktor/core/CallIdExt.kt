package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.features.CallId
import io.ktor.features.callId
import io.ktor.http.HttpHeaders
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import java.util.*

private const val NOT_SET = "NOT_SET"
private const val EXCLUDED_PATH = "EXCLUDED_PATH"

// Henter fra CorrelationID (backend tjenester)
fun CallId.Configuration.fromXCorrelationIdHeader(
        excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS
) {
    retrieve { call ->
        val headerValue = call.request.header(HttpHeaders.XCorrelationId)
        when (headerValue) {
            null -> {
                if (excludePaths.contains(call.request.path())) EXCLUDED_PATH // En path som ikke må ha Correlation ID
                else NOT_SET // Om headeren ikke er satt
            }
            EXCLUDED_PATH -> NOT_SET // Om noen har satt header til "EXCLUDE_PATH"
            else -> headerValue // Verdien fra headeren
        }
    }
}

// Genererer CorrelationID (frontend tjeneste)
fun CallId.Configuration.generated() {
    generate { UUID.randomUUID().toString() }
}

fun CallId.Configuration.ensureSet() {
    verify { callId: String ->
        if (callId == NOT_SET) {
            throw Throwblem(ValidationProblemDetails(
                    setOf(Violation(
                            parameterName = HttpHeaders.XCorrelationId,
                            parameterType = ParameterType.HEADER,
                            reason = "Correlation ID må settes.",
                            invalidValue = null
                    ))
            ))
        }
        true
    }
}

class Configuration
class RequireCallId(
        configure: Configuration
) {

    private suspend fun require(context: PipelineContext<Unit, ApplicationCall>) {
        context.context.callId ?: throw Throwblem(ValidationProblemDetails(
                setOf(Violation(
                        parameterName = HttpHeaders.XCorrelationId,
                        parameterType = ParameterType.HEADER,
                        reason = "Correlation ID må settes.",
                        invalidValue = null
                ))
        ))
    }

    companion object Feature :
            ApplicationFeature<ApplicationCallPipeline, Configuration, RequireCallId> {

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): RequireCallId {
            val result = RequireCallId(
                    Configuration().apply(configure)
            )

            pipeline.intercept(ApplicationCallPipeline.Call) {
                result.require(this)
            }

            return result
        }

        override val key = AttributeKey<RequireCallId>("RequireCallId")
    }
}