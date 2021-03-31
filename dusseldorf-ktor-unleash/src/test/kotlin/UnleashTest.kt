import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.silvercar.unleash.DefaultUnleash
import com.silvercar.unleash.FakeUnleash
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.unleash.unleashConfig
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UnleashTest {

    private companion object {
        // https://unleash.nais.io/#/features/view/dusseldorf-ktor-unleash-test-toggle
        private const val FEATURE_FLAG = "dusseldorf-ktor-unleash-test-toggle"

        private val logger: Logger = LoggerFactory.getLogger(UnleashTest::class.java)

        fun getConfig(config: Map<String, Any?> = TestConfiguration.asMap()): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(config)
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }
    }

    @Test
    internal fun `gitt at cluster er test, forvent en FakeUnleash`() {
        val unleash  = getConfig().unleashConfig()

        assertThat(unleash).isInstanceOf(FakeUnleash::class.java)
    }

    @Test
    internal fun `gitt at cluster er dev-gcp, forvent at feature flag eksisterer og er enabled`() {
        val unleash = getConfig(TestConfiguration.asMap(cluster = "dev-gcp")).unleashConfig()
        assertThat(unleash).isInstanceOf(DefaultUnleash::class.java)
        assertThat(unleash.featureToggleNames.first { it == FEATURE_FLAG }).isNotNull()
        assertThat(unleash.isEnabled(FEATURE_FLAG)).isTrue()
    }
}
