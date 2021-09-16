package no.nav.helse.dusseldorf.ktor.unleash

import com.typesafe.config.ConfigFactory
import io.ktor.config.*

internal object TestConfiguration {

    private fun configMap(
        port : Int = 8080,
        cluster: String,
        unleashAPI: String
    ) : Map<String, String> {

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.unleash.appName", "dusseldorf-ktor-unleash-test-app"),
            Pair("nav.unleash.instanceId", "dusseldorf-ktor-unleash-test-app-1"),
            Pair("nav.unleash.unleashAPI", unleashAPI),
            Pair("nav.unleash.synchronousFetchOnInitialisation", "false"),
            Pair("nav.unleash.fetchTogglesInterval", "2"),
            Pair("nav.unleash.sendMetricsInterval", "3"),
            Pair("nav.unleash.environment", cluster)
        )
        return map.toMap()
    }

    internal fun applicationConfig(
        cluster: String = "test",
        unleashAPI: String = "http://localhost:8080/unleash/api/"
    ) : ApplicationConfig = HoconApplicationConfig(ConfigFactory.parseMap(
        configMap(
        cluster = cluster,
        unleashAPI = unleashAPI
    )
    ))
}
