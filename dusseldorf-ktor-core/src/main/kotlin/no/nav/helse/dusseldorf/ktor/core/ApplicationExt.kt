package no.nav.helse.dusseldorf.ktor.core

import io.ktor.server.application.Application
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.ApplicationExt")

fun Application.logProxyProperties() {
    logger.info("------------------")
    logger.info("# Proxy Properties")
    val properties = System.getProperties()
    logger.info("## Fra System Properties")
    properties.forEach { key, value ->
        if (key is String && (key.startsWith(prefix = "http", ignoreCase = true) || key.startsWith(prefix = "https", ignoreCase = true))) {
            logger.info("$key=$value")
        }
    }
    logger.info("## Fra Environment Variables")
    val environmentVariables = System.getenv()
    logger.info("HTTP_PROXY=${environmentVariables["HTTP_PROXY"]}")
    logger.info("HTTPS_PROXY=${environmentVariables["HTTPS_PROXY"]}")
    logger.info("NO_PROXY=${environmentVariables["NO_PROXY"]}")
    logger.info("------------------")
}
