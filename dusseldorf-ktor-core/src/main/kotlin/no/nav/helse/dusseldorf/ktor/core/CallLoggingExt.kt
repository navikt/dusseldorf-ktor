package no.nav.helse.dusseldorf.ktor.core

import io.ktor.features.CallLogging
import io.ktor.features.callIdMdc
import io.ktor.http.HttpHeaders
import io.ktor.request.*
import io.ktor.response.header
import no.nav.helse.dusseldorf.ktor.core.IdVerifier.trimId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private val LOG: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.CallLoggingExt")

fun CallLogging.Configuration.correlationIdAndRequestIdInMdc() {
    callIdMdc("correlation_id")
    mdc("request_id") { call ->
        val requestId = when (val fraHeader = call.request.header(HttpHeaders.XRequestId)?.trimId()) {
            null -> IdVerifier.generate()
            else -> when (IdVerifier.verifyId(type = HttpHeaders.XRequestId, id = fraHeader)) {
                true -> fraHeader
                false -> IdVerifier.generate()
            }
        }
        call.response.header(HttpHeaders.XRequestId, requestId)
        requestId
    }
}

fun CallLogging.Configuration.logRequests(
        excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS,
        urlTemplate: Boolean = false
) {
    logger = LOG
    level = Level.INFO
    filter { call -> !excludePaths.contains(call.request.path()) }
    if(urlTemplate) format { applicationCall -> applicationCall.request.uri.toUrlTemplate() }
}

fun ApplicationRequest.log(
        verbose : Boolean = false,
        excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS,
        urlTemplate: Boolean = false
) {
    if (!excludePaths.contains(call.request.path())) {
        val uri = if(urlTemplate) this.uri.toUrlTemplate() else this.uri
        LOG.info("Request ${httpMethod.value} $uri (HTTP Version $httpVersion)")
        if (verbose) {
            LOG.info("Origin ${header(HttpHeaders.Origin)} (User Agent ${userAgent()})")
        }
    }
}

private fun String.toUrlTemplate(): String {
    val urlParts = split("?")

    val query = urlParts[1].split("&").joinToString("&") {
        it.replaceAfter("=", "{${it.substringBefore("=")}}")
    }

    return urlParts[0]+"?"+query
}