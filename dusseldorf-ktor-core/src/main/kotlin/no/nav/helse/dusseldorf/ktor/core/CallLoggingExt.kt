package no.nav.helse.dusseldorf.ktor.core

import io.ktor.features.CallLogging
import io.ktor.features.callIdMdc
import io.ktor.http.HttpHeaders
import io.ktor.request.*
import io.ktor.response.header
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.*

private val LOG: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.CallLoggingExt")

private const val GENERATED_REQUEST_ID_PREFIX = "generated-"

fun CallLogging.Configuration.correlationIdAndRequestIdInMdc() {
    callIdMdc("correlation_id")
    mdc("request_id") { call ->
        val requestId = call.request.header(HttpHeaders.XRequestId)?.removePrefix(GENERATED_REQUEST_ID_PREFIX) ?: "$GENERATED_REQUEST_ID_PREFIX${UUID.randomUUID()}"
        call.response.header(HttpHeaders.XRequestId, requestId)
        requestId
    }
}

fun CallLogging.Configuration.logRequests(
        excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS
) {
    logger = LOG
    level = Level.INFO
    filter { call -> !excludePaths.contains(call.request.path()) }
}

fun ApplicationRequest.log(
        verbose : Boolean = false,
        excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS
) {
    if (!excludePaths.contains(call.request.path())) {
        LOG.info("Request ${httpMethod.value} $uri (HTTP Version $httpVersion)")
        if (verbose) {
            LOG.info("Origin ${header(HttpHeaders.Origin)} (User Agent ${userAgent()})")
        }
    }
}