package no.nav.helse.dusseldorf.ktor.core

import io.ktor.features.CallId
import io.ktor.http.HttpHeaders
import java.util.*

private const val NOT_SET = "NOT_SET"

// Henter fra CorrelationID (backend tjenester)
fun CallId.Configuration.fromXCorrelationIdHeader() {
    retrieveFromHeader(HttpHeaders.XCorrelationId)
    generate { NOT_SET }
}

// Genererer CorrelationID (frontend tjeneste)
fun CallId.Configuration.generated() {
    generate { UUID.randomUUID().toString() }
}

fun CallId.Configuration.ensureSet() {
    verify { callId: String ->
        if (callId.isEmpty() || callId == NOT_SET) {
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