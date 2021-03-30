import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.silvercar.unleash.FakeUnleash
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.unleash.unleashConfig
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UnleashTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(UnleashTest::class.java)

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap()
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }
    }

    @Test
    internal fun `gitt at cluster er test, forvent en FakeUnleash`() {
        val config = getConfig()

        val unleash = config.unleashConfig()
        assertThat(unleash).isInstanceOf(FakeUnleash::class.java)
    }
}
