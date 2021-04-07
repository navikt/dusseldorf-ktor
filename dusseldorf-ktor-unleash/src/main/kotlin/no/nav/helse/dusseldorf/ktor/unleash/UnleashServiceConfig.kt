package no.nav.helse.dusseldorf.ktor.unleash

import io.ktor.config.*
import no.finn.unleash.UnleashContext
import no.finn.unleash.util.UnleashConfig
import no.nav.helse.dusseldorf.ktor.core.EnvironmentUtils
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.unleash.UnleashConfig")

fun ApplicationConfig.unleashConfigBuilder(
    path: String = "nav.unleash",
    synchronousFetchOnInitialisation: Boolean = true,
    fetchTogglesInterval: Long = 1,
    sendMetricsInterval: Long = 1
): UnleashConfig.Builder {

    val appName: String =
        EnvironmentUtils.applicationName ?: getOptionalString("${path}.app_name", false) ?: throw IllegalStateException(
            "unleashConfig appName må være satt."
        )

    val instanceId: String = EnvironmentUtils.podId ?: getOptionalString("${path}.instance_id", false) ?: throw IllegalStateException(
        "unleashConfig instanceId må være satt."
    )

    val environment: String = EnvironmentUtils.clusterName ?: getOptionalString("${path}.cluster", false) ?: throw IllegalStateException(
        "unleashConfig environment må være satt."
    )

    val apiUrl = URI(getOptionalString("${path}.api_url", false) ?: "https://unleash.nais.io/api/")

    return UnleashConfig.builder()
        .appName(appName)
        .instanceId(instanceId)
        .environment(environment)
        .unleashAPI(apiUrl)
        .synchronousFetchOnInitialisation(synchronousFetchOnInitialisation)
        .fetchTogglesInterval(fetchTogglesInterval)
        .sendMetricsInterval(sendMetricsInterval)
        .unleashContextProvider { UnleashContext.builder().appName(appName).build() }
}

