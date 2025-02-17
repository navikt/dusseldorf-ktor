package no.nav.helse.dusseldorf.ktor.core

import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import io.ktor.server.plugins.callid.CallIdConfig
import io.ktor.server.request.header
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

internal object IdVerifier {
    private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.IdVerifier")
    private const val NorskeBokstaver = "æøåÆØÅ"
    private const val GeneratedIdPrefix = "generated-"

    private val idRegex = "[a-zA-Z0-9_.\\-${NorskeBokstaver}]{5,200}".toRegex()
    internal fun verifyId(type: String, id: String) = idRegex.matches(id).also { valid ->
        if (!valid) logger.warn("Ugyldig $type=[${id.encodeURLParameter()}] (url-encoded)")
    }

    internal fun generate() = "$GeneratedIdPrefix${UUID.randomUUID()}"
    internal fun String.trimId() = removePrefix(GeneratedIdPrefix)
}

fun CallIdConfig.fromFirstNonNullHeader(
    headers: List<String>,
    generateOnInvalid: Boolean = false,
    generateOnNotSet: Boolean = false,
) {
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
fun CallIdConfig.fromXCorrelationIdHeader(generateOnInvalid: Boolean = false, generateOnNotSet: Boolean = false) =
    fromFirstNonNullHeader(
        headers = listOf(HttpHeaders.XCorrelationId), generateOnInvalid = generateOnInvalid, generateOnNotSet = false
    )

// Genererer CorrelationID (frontend tjeneste)
fun CallIdConfig.generated() {
    generate { IdVerifier.generate() }
}
