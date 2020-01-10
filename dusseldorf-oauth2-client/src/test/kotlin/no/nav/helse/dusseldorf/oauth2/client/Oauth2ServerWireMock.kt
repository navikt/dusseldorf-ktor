package no.nav.helse.dusseldorf.oauth2.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import java.net.URI
import java.util.*

private val tokenEndpoint = WireMock.urlPathMatching(".*/token.*")

class Oauth2ServerWireMock {

    private val wireMockServer : WireMockServer

    init {
        val wireMockConfiguration = WireMockConfiguration.options()
        wireMockConfiguration.dynamicPort()

        val wireMockServer = WireMockServer(wireMockConfiguration)

        wireMockServer.start()
        WireMock.configureFor(wireMockServer.port())

        this.wireMockServer = wireMockServer
    }

    fun stop() {
        wireMockServer.stop()
    }

    fun getTokenUrl() : URI {
        return URI(wireMockServer.baseUrl() + "/token")
    }

    fun stubGetTokenSignedJwtClientCredentials(
            expiresIn : Long = 3600,
            accessToken: String = "GetTokenSignedJwtClientCredentials"
    ) {
        stubGetTokenSignedJwt(
                clientAssertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                grantType = "client_credentials",
                accessToken = accessToken,
                expiresIn = expiresIn
        )
    }

    fun stubGetTokenClientSecretClientCredentials(
            clientId : String,
            clientSecret: String,
            expiresIn : Long = 3600,
            accessToken: String = "GetTokenClientSecretClientCredentials"
    ) {
        stubGetTokenClientSecret(
                clientId = clientId,
                clientSecret = clientSecret,
                grantType = "client_credentials",
                accessToken = accessToken,
                expiresIn = expiresIn
        )
    }

    private fun stubGetTokenClientSecret(
            clientId: String,
            clientSecret: String,
            grantType : String,
            accessToken: String,
            expiresIn: Long
    ) {

        WireMock.stubFor(
                WireMock.post(tokenEndpoint)
                        .withHeader(HttpHeaders.Authorization, WireMock.equalTo("Basic " + Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())))
                        .withRequestBody(WireMock.containing("grant_type=${grantType.encodeURLParameter()}"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(response(accessToken, expiresIn))
                        )
        )
    }

    private fun stubGetTokenSignedJwt(
            clientAssertionType : String,
            grantType : String,
            accessToken: String,
            expiresIn: Long
    ) {
        WireMock.stubFor(
                WireMock.post(tokenEndpoint)
                        .withRequestBody(WireMock.containing("client_assertion_type=${clientAssertionType.encodeURLParameter()}"))
                        .withRequestBody(WireMock.containing("grant_type=${grantType.encodeURLParameter()}"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(response(accessToken, expiresIn))
                        )
        )
    }

    private fun response(
            accessToken: String,
            expiresIn : Long
    ) : String = """
    {
        "access_token" : "$accessToken",
        "expires_in": $expiresIn,
        "token_type": "Bearer"
    }
    """.trimIndent()
}