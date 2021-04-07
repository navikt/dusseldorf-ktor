import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import kotlinx.coroutines.runBlocking
import no.finn.unleash.FakeUnleash
import no.nav.helse.dusseldorf.ktor.core.EnvironmentUtils
import no.nav.helse.dusseldorf.ktor.core.EnvironmentUtils.NAIS_CLUSTER_NAME_PROPERTY_NAME
import no.nav.helse.dusseldorf.ktor.core.EnvironmentUtils.Type.PUBLIC
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.unleash.UnleashService
import no.nav.helse.dusseldorf.ktor.unleash.unleashConfigBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import kotlin.test.assertFailsWith

class UnleashServiceTest {

    private companion object {
        // https://unleash.nais.io/#/features/view/dusseldorf-ktor-unleash-test-toggle
        private const val FEATURE_FLAG = "dusseldorf-ktor-unleash-test-toggle"

        private val logger: Logger = LoggerFactory.getLogger(UnleashServiceTest::class.java)

        fun getConfig(config: Map<String, Any?> = TestConfiguration.asMap()): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(config)
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }

        private val unleashConfigBuilder = getConfig(TestConfiguration.asMap(cluster = "dev-gcp")).unleashConfigBuilder()
    }

    @Test
    internal fun `gitt at unleash klient er aktivert for dev-gcp, forvent at feature flag eksisterer og er enabled`() {
        EnvironmentUtils.setProperty(NAIS_CLUSTER_NAME_PROPERTY_NAME, "dev-gcp", PUBLIC)
        val unleashService = UnleashService(unleashConfigBuilder)

        assertThat(unleashService.more().featureToggleNames.first { it == FEATURE_FLAG }).isNotNull()
        assertThat(unleashService.isEnabled(FEATURE_FLAG)).isTrue()
    }

    @Test
    internal fun `gitt at feature_flag ikke aktivert for gyldig cluster, forvent at flagg er deaktivert`() {
        EnvironmentUtils.setProperty(NAIS_CLUSTER_NAME_PROPERTY_NAME, "ugyldig-cluster", PUBLIC)
        val unleashService = UnleashService(unleashConfigBuilder)
        assertThat(unleashService.isEnabled(FEATURE_FLAG)).isFalse()
    }

    @Test
    internal fun `gitt at unleash server ikke er tilgjengelig, forvent unhealthy status`() {

        val unleashService = UnleashService(unleashConfigBuilder.unleashAPI("http://localhost:8081/api/"))
        unleashService.isEnabled("some.toggle")

        val result: Result = runBlocking { unleashService.check() }
        assertThat(result).isInstanceOf(UnHealthy::class.java)
    }

    @Test
    internal fun `gitt at unleashConfigBuilder, forvent at UnleashService bruker FakeUnleash`() {

        val unleashService = UnleashService()
        assertDoesNotThrow { unleashService.fakeUnleash() }
        assertThat(unleashService.fakeUnleash()).isInstanceOf(FakeUnleash::class.java)
    }

    @Test
    internal fun `gitt gyldig unleashConfigBuilder, forvent IllegalStateException ved kall på fakeUnleash()`() {

        val unleashService = UnleashService(unleashConfigBuilder)
        assertFailsWith(IllegalStateException::class) {unleashService.fakeUnleash()}
    }
}
