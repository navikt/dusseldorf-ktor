package no.nav.helse.dusseldorf.ktor.unleash

import no.nav.helse.dusseldorf.ktor.unleash.TestConfiguration.applicationConfig
import kotlinx.coroutines.runBlocking
import io.getunleash.repository.FeatureToggleResponse
import io.getunleash.FakeUnleash
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.IllegalStateException
import kotlin.test.assertTrue

internal class UnleashServiceTest {

    private enum class Feature: UnleashFeature {
        // https://unleash.nais.io/#/features/view/dusseldorf-ktor-unleash-test-toggle
        DUSSELDORF_KTOR_UNLEASH_TEST_TOGGLE {
            override fun featureName(): String = "dusseldorf-ktor-unleash-test-toggle"
        },

        SOME_FLAG {
            override fun featureName(): String = "some.toggle"

        }
    }

    @Test
    fun `henter fra application config`() {
        val unleashConfig = applicationConfig(
            cluster = "mitt-cluster",
            unleashAPI = "http://localhost:8081/api/"
        ).unleashConfigBuilder().build()
        assertEquals("mitt-cluster", unleashConfig.environment)
        assertEquals("http://localhost:8081/api/", unleashConfig.unleashAPI.toString())
        assertEquals("dusseldorf-ktor-unleash-test-app", unleashConfig.appName)
        assertEquals("dusseldorf-ktor-unleash-test-app-1", unleashConfig.instanceId)
        assertFalse(unleashConfig.isSynchronousFetchOnInitialisation)
        assertEquals(2L, unleashConfig.fetchTogglesInterval)
        assertEquals(3L, unleashConfig.sendMetricsInterval)
    }

    @Test
    @Disabled
    fun `gitt at unleash klient er aktivert for dev-gcp, forvent at feature flag eksisterer og er enabled`() {
        val unleashConfigBuilder = applicationConfig(cluster = "dev-gcp", unleashAPI = "https://unleash.nais.io/").unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)

        assertNotNull(unleashService.more().featureToggleNames.first { it == Feature.DUSSELDORF_KTOR_UNLEASH_TEST_TOGGLE.featureName() })
        assertTrue(unleashService.isEnabled(Feature.DUSSELDORF_KTOR_UNLEASH_TEST_TOGGLE, false))
    }

    @Test
    @Disabled
    fun `gitt at feature_flag ikke aktivert for gyldig cluster, forvent at flagg er deaktivert`() {
        val unleashConfigBuilder = applicationConfig(cluster = "ugyldig-cluster", unleashAPI = "https://unleash.nais.io/").unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)
        assertFalse(unleashService.isEnabled(Feature.DUSSELDORF_KTOR_UNLEASH_TEST_TOGGLE, true))
    }

    @Test
    fun `gitt at unleash server ikke er tilgjengelig, forvent unhealthy status`() {
        val unleashConfigBuilder = applicationConfig(cluster = "dev-gcp", unleashAPI = "http://localhost:8081/api/").unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)
        unleashService.togglesFetched(FeatureToggleResponse(FeatureToggleResponse.Status.UNAVAILABLE, 503))
        unleashService.isEnabled(Feature.SOME_FLAG, false)

        val result: Result = runBlocking { unleashService.check() }
        assertInstanceOf(UnHealthy::class.java, result)
    }

    @Test
    fun `gitt at unleashConfigBuilder er null, forvent at UnleashService bruker FakeUnleash`() {

        val unleashService = UnleashService()
        assertDoesNotThrow { unleashService.fakeUnleash() }
        assertInstanceOf(FakeUnleash::class.java, unleashService.fakeUnleash())
    }

    @Test
    fun `gitt gyldig unleashConfigBuilder, forvent IllegalStateException ved kall på fakeUnleash()`() {
        val unleashConfigBuilder = applicationConfig().unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)
        assertThrows<IllegalStateException> {
            unleashService.fakeUnleash()
        }
    }
}
