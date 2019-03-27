package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.features.CallId
import io.ktor.features.callId
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import java.util.*

// Henter fra CorrelationID (backend tjenester)
fun CallId.Configuration.fromXCorrelationIdHeader() {
    retrieveFromHeader(HttpHeaders.XCorrelationId)
}

// Genererer CorrelationID (frontend tjeneste)
fun CallId.Configuration.generated() {
    generate { UUID.randomUUID().toString() }
}

class Configuration {
    var excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS
}
class CallIdRequired(
        private val configure: Configuration
) {

    private val problemDetails = ValidationProblemDetails(
            setOf(Violation(
                    parameterName = HttpHeaders.XCorrelationId,
                    parameterType = ParameterType.HEADER,
                    reason = "Correlation ID må settes.",
                    invalidValue = null
            ))
    )

    private val status = HttpStatusCode.fromValue(problemDetails.status)

    private suspend fun require(context: PipelineContext<Unit, ApplicationCall>) {
        val callId = context.context.callId
        if (callId == null && !configure.excludePaths.contains(context.context.request.path())) {
            context.context.respond(
                status = status,
                message = problemDetails
            )
            context.finish()
        } else {
            context.proceed()
        }
    }

    companion object Feature :
            ApplicationFeature<ApplicationCallPipeline, Configuration, CallIdRequired> {

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): CallIdRequired {
            val result = CallIdRequired(
                    Configuration().apply(configure)
            )

            pipeline.intercept(ApplicationCallPipeline.Call) {
                result.require(this)
            }

            return result
        }

        override val key = AttributeKey<CallIdRequired>("CallIdRequired")
    }
}