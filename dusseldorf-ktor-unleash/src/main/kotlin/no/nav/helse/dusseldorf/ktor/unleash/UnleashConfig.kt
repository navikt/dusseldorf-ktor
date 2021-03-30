package no.nav.helse.dusseldorf.ktor.unleash

import com.silvercar.unleash.DefaultUnleash
import com.silvercar.unleash.FakeUnleash
import com.silvercar.unleash.Unleash
import com.silvercar.unleash.event.UnleashReady
import com.silvercar.unleash.event.UnleashSubscriber
import com.silvercar.unleash.repository.FeatureToggleResponse
import com.silvercar.unleash.repository.ToggleCollection
import com.silvercar.unleash.strategy.Strategy
import com.silvercar.unleash.util.UnleashConfig
import com.silvercar.unleash.util.unleashConfig
import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.unleash.UnleashConfig")

fun ApplicationConfig.unleashConfig(path: String = "nav.unleash", vararg strategies: Strategy): Unleash {

    val environment = getRequiredString("${path}.cluster", false)
    if (environment == "test" || environment == "local") return FakeUnleash()

    val appName = getRequiredString("${path}.app_name", false)
    val instanceId = getRequiredString("${path}.instance_id", false)
    val apiUrl = URI(getRequiredString("${path}.api_url", false))

    val config: UnleashConfig = unleashConfig {
        appName(appName)
        instanceId(instanceId)
        unleashAPI(apiUrl)
        this.
        subscriber(object : UnleashSubscriber {
            override fun onReady(ready: UnleashReady) {
                logger.info("Unleash is ready")
            }

            override fun togglesFetched(response: FeatureToggleResponse) {
                logger.info("Fetch toggles with status: " + response.status)
            }

            override fun togglesBackedUp(toggleCollection: ToggleCollection) {
                logger.info("Backup stored.")
            }
        })
    }.build()

    return DefaultUnleash(
        unleashConfig = config,
        strategies = strategies
    )
}
