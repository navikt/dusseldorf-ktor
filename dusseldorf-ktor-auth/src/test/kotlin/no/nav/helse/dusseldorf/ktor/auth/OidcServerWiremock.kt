package no.nav.helse.dusseldorf.ktor.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

private val path = "/.well-known"

class OidcServerWiremock {

    private val wireMockServer : WireMockServer

    init {
        val wireMockConfiguration = WireMockConfiguration.options()
        wireMockConfiguration.dynamicPort()

        val wireMockServer = WireMockServer(wireMockConfiguration)

        wireMockServer.start()
        WireMock.configureFor(wireMockServer.port())

        stubWellKnown(path, """
            {
                "issuer": "http://localhost:8080",
                "jwks_uri": "http://localhost:8080/jwks_uri",
                "token_endpoint": "http://localhost:8080/token"
            }
        """.trimIndent())

        this.wireMockServer = wireMockServer
    }

    fun getValidDiscoveryEndpoint() = wireMockServer.baseUrl() + path

    private fun stubWellKnown(
            path: String,
            response: String
    ) {
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*$path"))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(response)
                    )
        )
    }
}