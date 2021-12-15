package no.nav.helse.dusseldorf.ktor.unleash

import io.ktor.config.*
import io.getunleash.UnleashContext
import io.getunleash.util.UnleashConfig
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import java.net.URI

fun ApplicationConfig.unleashConfigBuilder(
    path: String = "nav.unleash"
): UnleashConfig.Builder {

    val appName = getOptionalString("$path.appName", false) ?: getRequiredString("NAIS_APP_NAME", false)
    val instanceId = getOptionalString("$path.instanceId", false) ?: getRequiredString("HOSTNAME", false)
    val environment = getOptionalString("$path.environment", false) ?: getRequiredString("NAIS_CLUSTER_NAME", false)
    val synchronousFetchOnInitialisation = getOptionalString("$path.synchronousFetchOnInitialisation", false)?.toBoolean() ?: true
    val fetchTogglesInterval = getOptionalString("$path.fetchTogglesInterval", false)?.toLong() ?: 1
    val sendMetricsInterval = getOptionalString("$path.sendMetricsInterval", false)?.toLong() ?: 1
    val unleashAPI = URI(getOptionalString("$path.unleashAPI", false) ?: "https://unleash.nais.io/api/")

    return UnleashConfig.builder()
        .appName(appName)
        .instanceId(instanceId)
        .environment(environment)
        .unleashAPI(unleashAPI)
        .synchronousFetchOnInitialisation(synchronousFetchOnInitialisation)
        .fetchTogglesInterval(fetchTogglesInterval)
        .sendMetricsInterval(sendMetricsInterval)
        .unleashContextProvider { UnleashContext.builder().appName(appName).build() }
}

