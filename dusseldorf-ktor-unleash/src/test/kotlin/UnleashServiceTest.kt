import TestConfiguration.applicationConfig
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import kotlinx.coroutines.runBlocking
import no.finn.unleash.FakeUnleash
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.unleash.UnleashFeature
import no.nav.helse.dusseldorf.ktor.unleash.UnleashService
import no.nav.helse.dusseldorf.ktor.unleash.unleashConfigBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class UnleashServiceTest {

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
    internal fun `henter fra application config`() {
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
    internal fun `gitt at unleash klient er aktivert for dev-gcp, forvent at feature flag eksisterer og er enabled`() {
        val unleashConfigBuilder = applicationConfig(cluster = "dev-gcp").unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)

        assertThat(unleashService.more().featureToggleNames.first { it == Feature.DUSSELDORF_KTOR_UNLEASH_TEST_TOGGLE.featureName() }).isNotNull()
        assertThat(unleashService.isEnabled(Feature.DUSSELDORF_KTOR_UNLEASH_TEST_TOGGLE, false)).isTrue()
    }

    @Test
    internal fun `gitt at feature_flag ikke aktivert for gyldig cluster, forvent at flagg er deaktivert`() {
        val unleashConfigBuilder = applicationConfig(cluster = "ugyldig-cluster").unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)
        assertThat(unleashService.isEnabled(Feature.DUSSELDORF_KTOR_UNLEASH_TEST_TOGGLE, true)).isFalse()
    }

    @Test
    internal fun `gitt at unleash server ikke er tilgjengelig, forvent unhealthy status`() {
        val unleashConfigBuilder = applicationConfig(cluster = "dev-gcp", unleashAPI = "http://localhost:8081/api/").unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)
        unleashService.isEnabled(Feature.SOME_FLAG, true)

        val result: Result = runBlocking { unleashService.check() }
        assertThat(result).isInstanceOf(UnHealthy::class.java)
    }

    @Test
    internal fun `gitt at unleashConfigBuilder er null, forvent at UnleashService bruker FakeUnleash`() {

        val unleashService = UnleashService()
        assertDoesNotThrow { unleashService.fakeUnleash() }
        assertThat(unleashService.fakeUnleash()).isInstanceOf(FakeUnleash::class.java)
    }

    @Test
    internal fun `gitt gyldig unleashConfigBuilder, forvent IllegalStateException ved kall på fakeUnleash()`() {
        val unleashConfigBuilder = applicationConfig().unleashConfigBuilder()
        val unleashService = UnleashService(unleashConfigBuilder)
        assertFailsWith(IllegalStateException::class) {unleashService.fakeUnleash()}
    }
}
