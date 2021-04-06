package no.nav.helse.dusseldorf.ktor.unleash

import io.ktor.config.*
import no.finn.unleash.*
import no.finn.unleash.event.UnleashReady
import no.finn.unleash.event.UnleashSubscriber
import no.finn.unleash.repository.FeatureToggleResponse
import no.finn.unleash.repository.ToggleCollection
import no.finn.unleash.strategy.Strategy
import no.finn.unleash.util.UnleashConfig
import no.nav.common.featuretoggle.ByClusterStrategy
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.unleash.UnleashConfig")

private val NOT_LOCAL_ENVIRONMENTS = listOf(
    "dev-sbs", "dev-fss", "dev-gcp",
    "prod-sbs", "prod-fss", "prod-gcp",
)

fun ApplicationConfig.unleashConfig(
    path: String = "nav.unleash",
    synchronousFetchOnInitialisation: Boolean = true,
    fetchTogglesInterval: Long = 1,
    sendMetricsInterval: Long = 1,
    subscriber: UnleashSubscriber = object : UnleashSubscriber {
        override fun onReady(ready: UnleashReady) {
            logger.info("Unleash is ready")
        }

        override fun togglesFetched(response: FeatureToggleResponse) {
            logger.info("Fetch toggles with status: " + response.status)
        }

        override fun togglesBackedUp(toggleCollection: ToggleCollection) {
            logger.info("Backup stored.")
        }
    },
    vararg strategies: Strategy
): Unleash {

    val environment = getOptionalString("${path}.cluster", false) ?: System.getenv("NAIS_CLUSTER_NAME")
    if (!NOT_LOCAL_ENVIRONMENTS.contains(environment)) return FakeUnleash()

    val appName = getOptionalString("${path}.app_name", false) ?: System.getenv("NAIS_APP_NAME")
    val instanceId = getOptionalString("${path}.instance_id", false) ?: System.getenv("HOSTNAME")
    val apiUrl = URI(getOptionalString("${path}.api_url", false) ?: "https://unleash.nais.io/api/")

    val config = UnleashConfig.builder()
        .appName(appName)
        .instanceId(instanceId)
        .unleashAPI(apiUrl)
        .synchronousFetchOnInitialisation(synchronousFetchOnInitialisation)
        .fetchTogglesInterval(fetchTogglesInterval)
        .sendMetricsInterval(sendMetricsInterval)
        .unleashContextProvider { UnleashContext.builder().appName(appName).build() }
        .subscriber(subscriber)
        .build()

    return DefaultUnleash(config, ByClusterStrategy(), *strategies)
}
