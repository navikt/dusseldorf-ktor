package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.features.CallId
import io.ktor.features.callId
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import io.ktor.request.header
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

internal object IdVerifier {
    private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.IdVerifier")
    private const val NorskeBokstaver = "æøåÆØÅ"
    private const val GeneratedIdPrefix = "generated-"

    private val idRegex  = "[a-zA-Z0-9_.\\-${NorskeBokstaver}]{5,200}".toRegex()
    internal fun verifyId(type: String, id:String) = idRegex.matches(id).also { valid ->
        if (!valid) logger.warn("Ugyldig $type=[${id.encodeURLParameter()}] (url-encoded)")
    }
    internal fun generate() = "$GeneratedIdPrefix${UUID.randomUUID()}"
    internal fun String.trimId() = removePrefix(GeneratedIdPrefix)
}

fun CallId.Configuration.fromFirstNonNullHeader(headers: List<String>, generateOnInvalid: Boolean = false, generateOnNotSet: Boolean = false) {
    retrieve { call ->
        when (val fromHeaders = headers.mapNotNull { call.request.header(it) }.firstOrNull()) {
            null -> when (generateOnNotSet) {
                true -> IdVerifier.generate()
                false -> fromHeaders
            }
            else -> when (IdVerifier.verifyId(type = HttpHeaders.XCorrelationId, id = fromHeaders)) {
                true -> fromHeaders
                false -> when (generateOnInvalid) {
                    true -> IdVerifier.generate()
                    false -> fromHeaders
                }
            }
        }
    }

    verify { IdVerifier.verifyId(type = HttpHeaders.XCorrelationId, id = it) }
}

// Henter fra CorrelationID (backend tjenester)
fun CallId.Configuration.fromXCorrelationIdHeader(generateOnInvalid: Boolean = false, generateOnNotSet: Boolean = false) = fromFirstNonNullHeader(
    headers = listOf(HttpHeaders.XCorrelationId), generateOnInvalid = generateOnInvalid, generateOnNotSet = false
)

// Genererer CorrelationID (frontend tjeneste)
fun CallId.Configuration.generated() {
    generate { IdVerifier.generate() }
}

class Configuration
class CallIdRequired(private val configure: Configuration) {

    private val logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.CallIdRequired")

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
            context.context.respondProblemDetails(problemDetails, logger)
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
