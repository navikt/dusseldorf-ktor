package no.nav.helse.dusseldorf.ktor.core

import io.ktor.features.CallId
import io.ktor.http.HttpHeaders
import java.util.*

// Henter fra CorrelationID (backend tjenester)
fun CallId.Configuration.fromXCorrelationIdHeader() {
    header(HttpHeaders.XCorrelationId)
}

// Genererer CorrelationID (frontend tjeneste)
fun CallId.Configuration.generated() {
    generate { UUID.randomUUID().toString() }
}

fun CallId.Configuration.ensureSet() {
    verify { callId: String ->
        if (callId.isEmpty()) {
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