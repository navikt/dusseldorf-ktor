package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.features.CallId
import io.ktor.features.callId
import io.ktor.http.HttpHeaders
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

class Configuration
class CallIdRequired(private val configure: Configuration) {

    private val problemDetails = ValidationProblemDetails(
            setOf(Violation(
                    parameterName = HttpHeaders.XCorrelationId,
                    parameterType = ParameterType.HEADER,
                    reason = "Correlation ID må settes.",
                    invalidValue = null
            ))
    )

    fun interceptPipeline(pipeline: ApplicationCallPipeline) {
        pipeline.intercept(ApplicationCallPipeline.Monitoring) {
            require(this)
        }
    }

    private suspend fun require(context: PipelineContext<Unit, ApplicationCall>) {
        val callId = context.context.callId
        if (callId == null) {
            context.context.respondProblemDetails(problemDetails)
            context.finish()
        } else {
            context.proceed()
        }
    }

    companion object Feature :
            ApplicationFeature<ApplicationCallPipeline, Configuration, CallIdRequired> {

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): CallIdRequired {
            return CallIdRequired(Configuration().apply(configure))
        }

        override val key = AttributeKey<CallIdRequired>("CallIdRequired")
    }
}