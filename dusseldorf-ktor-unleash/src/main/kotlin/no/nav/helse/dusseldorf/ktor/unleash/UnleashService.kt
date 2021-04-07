package no.nav.helse.dusseldorf.ktor.unleash

import no.finn.unleash.*
import no.finn.unleash.event.UnleashSubscriber
import no.finn.unleash.repository.FeatureToggleResponse
import no.finn.unleash.strategy.Strategy
import no.finn.unleash.util.UnleashConfig
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.unleash.strategy.ByClusterStrategy
import org.slf4j.LoggerFactory

/**
 * Serviceklasse for konfigurering av unleash klient.
 *
 * @constructor unleashBuilder: Bruker en DefaultUnleash klient dersom builder er gyldig. Dersom den er null brukes det en FakeUnleash.
 * @constructor strategies: En liste med strategier å bruke. Default brukes [ByClusterStrategy]
 */
data class UnleashService(
    val unleashBuilder: UnleashConfig.Builder? = null,
    val strategies: List<Strategy> = listOf(ByClusterStrategy())
) : HealthCheck, UnleashSubscriber {

    private companion object {
        private val logger = LoggerFactory.getLogger(UnleashService::class.java)
    }

    private var lastTogglesFetchedStatus: FeatureToggleResponse.Status? = null
    private val unleash: Unleash = when (unleashBuilder) {
        null -> FakeUnleash()
        else -> DefaultUnleash(
            unleashBuilder.subscriber(this).build(),
            *strategies.toTypedArray()
        )
    }

    fun isEnabled(toggleName: String): Boolean {
        return unleash.isEnabled(toggleName)
    }

    fun isEnabled(toggleName: String, unleashContext: UnleashContext): Boolean {
        return unleash.isEnabled(toggleName, unleashContext)
    }

    fun more(): MoreOperations {
        return unleash.more()
    }

    fun fakeUnleash(): FakeUnleash {
        if (unleash is FakeUnleash) return unleash
        else throw IllegalStateException("Instanse av unleash er ikke FakeUnleash")
    }

    override fun togglesFetched(toggleResponse: FeatureToggleResponse) {
        lastTogglesFetchedStatus = toggleResponse.status
    }

    override fun onError(unleashException: UnleashException) {
        logger.warn(unleashException.message, unleashException)
    }

    override suspend fun check(): Result {
        return when (lastTogglesFetchedStatus) {
            FeatureToggleResponse.Status.CHANGED, FeatureToggleResponse.Status.NOT_CHANGED -> {
                Healthy("Henting av feature toggles, OK", lastTogglesFetchedStatus!!)
            }
            else -> {
                UnHealthy("Feil ved henting av feature toggles.", "null")
            }
        }
    }
}
