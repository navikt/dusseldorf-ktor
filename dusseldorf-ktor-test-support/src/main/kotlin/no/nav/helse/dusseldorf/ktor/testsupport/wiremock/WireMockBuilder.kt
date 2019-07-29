package no.nav.helse.dusseldorf.ktor.testsupport.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.helse.dusseldorf.ktor.testsupport.jws.Azure

class WireMockBuilder {

    private companion object {
        private const val AZURE_V1_TRANSFORMER = "azure-v1"
        private const val AZURE_V2_TRANSFORMER = "azure-v2"
    }

    private val config = WireMockConfiguration.options()
    private var port: Int? = null
    private var serverFunction : ((wireMockServer: WireMockServer) -> Unit)? = null
    private var configFunction : ((wireMockConfiguration: WireMockConfiguration) -> Unit)? = null
    private var withAzureSupport = false


    fun withPort(port: Int) : WireMockBuilder {
        this.port = port
        return this
    }

    fun wireMockConfiguration(block: (wireMockConfiguration: WireMockConfiguration) -> Unit) : WireMockBuilder {
        this.configFunction = block
        return this
    }

    fun wireMockServer(block: (wireMockServer: WireMockServer) -> Unit) : WireMockBuilder {
        this.serverFunction = block
        return this
    }

    fun withAzureSupport() : WireMockBuilder {
        val azureV1 = AzureTokenResponseTransformer(name = AZURE_V1_TRANSFORMER, accessTokenGenerator = { clientId, audience, scopes ->
            Azure.V1_0.generateJwt(clientId, audience)
        })
        val azureV2 = AzureTokenResponseTransformer(name = AZURE_V2_TRANSFORMER, accessTokenGenerator = { clientId, audience, scopes ->
            Azure.V2_0.generateJwt(clientId, audience)
        })

        config.extensions(azureV1, azureV2)
        withAzureSupport = true
        return this
    }

    private fun addAzureStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching(".*${Paths.AZURE_V1_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(AZURE_V1_TRANSFORMER)))
        WireMockStubs.stubJwks(Paths.AZURE_V1_JWKS_PATH)
        WireMockStubs.stubWellKnown(
                path = Paths.AZURE_V1_WELL_KNOWN_PATH,
                issuer = Azure.V1_0.getIssuer(),
                jwkSetUrl = server.getAzureV1JwksUrl(),
                tokenEndpoint = server.getAzureV1TokenUrl()
        )

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching(".*${Paths.AZURE_V2_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(AZURE_V2_TRANSFORMER)))
        WireMockStubs.stubJwks(Paths.AZURE_V2_JWKS_PATH)
        WireMockStubs.stubWellKnown(
                path = Paths.AZURE_V2_WELL_KNOWN_PATH,
                issuer = Azure.V2_0.getIssuer(),
                jwkSetUrl = server.getAzureV2JwksUrl(),
                tokenEndpoint = server.getAzureV2TokenUrl()
        )
    }

    fun build() : WireMockServer {
        if (port == null) config.dynamicPort()
        else config.port(port!!)

        configFunction?.invoke(config)
        val server = WireMockServer(config)
        serverFunction?.invoke(server)

        server.start()
        WireMock.configureFor(server.port())

        if (withAzureSupport) addAzureStubs(server)
        return server
    }
}