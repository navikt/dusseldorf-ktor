package no.nav.helse.dusseldorf.ktor.core

import io.ktor.features.CallLogging
import io.ktor.features.callIdMdc
import io.ktor.http.HttpHeaders
import io.ktor.request.*
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
        requestId
    }
}

fun CallLogging.Configuration.logRequests(
        excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS,
        templateQueryParameters: Boolean = false
) {
    logger = LOG
    level = Level.INFO
    filter { call -> !excludePaths.contains(call.request.path()) }
    if(templateQueryParameters) format { applicationCall -> applicationCall.request.uri.templateQueryParameters() }
}

fun ApplicationRequest.log(
        verbose : Boolean = false,
        excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS,
        templateQueryParameters: Boolean = false
) {
    if (!excludePaths.contains(call.request.path())) {
        val uri = if(templateQueryParameters) this.uri.templateQueryParameters() else this.uri
        LOG.info("Request ${httpMethod.value} $uri (HTTP Version $httpVersion)")
        if (verbose) {
            LOG.info("Origin ${header(HttpHeaders.Origin)} (User Agent ${userAgent()})")
        }
    }
}

fun String.templateQueryParameters(): String {
    val urlParts = split("?")
    if(urlParts.size <2) return this

    val query = urlParts[1].split("&").joinToString("&") {
        it.replaceAfter("=", "{${it.substringBefore("=")}}")
    }

    return urlParts[0]+"?"+query
}