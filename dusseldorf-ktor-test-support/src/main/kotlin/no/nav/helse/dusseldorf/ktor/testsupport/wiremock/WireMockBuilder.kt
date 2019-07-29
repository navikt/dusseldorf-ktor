package no.nav.helse.dusseldorf.ktor.testsupport.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.helse.dusseldorf.ktor.testsupport.jws.Azure
import no.nav.helse.dusseldorf.ktor.testsupport.jws.LoginService

class WireMockBuilder {

    private companion object {
        private const val AZURE_V1_TRANSFORMER = "azure-v1"
        private const val AZURE_V2_TRANSFORMER = "azure-v2"
        private const val LOGIN_SERVICE_V1_TRANSFORMER = "login-service-v1"
        private const val NAIS_STS_TRANSFORMER = "nais-sts"
        private const val NAIS_STS_ISSUER = "http://localhost:8080/nais-sts"
    }

    private val config = WireMockConfiguration.options()
    private var port: Int? = null
    private var serverFunction : ((wireMockServer: WireMockServer) -> Unit)? = null
    private var configFunction : ((wireMockConfiguration: WireMockConfiguration) -> Unit)? = null
    private var withAzureSupport = false
    private var withLoginServieSupport = false
    private var withNaisStsSupport = false

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

    fun withLoginServiceSupport(cookieName: String = "localhost-idtoken") : WireMockBuilder {
        val loginServiceV1 = LoginServiceLoginResponseTransformer(name = LOGIN_SERVICE_V1_TRANSFORMER, cookieName = cookieName)
        config.extensions(loginServiceV1)
        withLoginServieSupport = true
        return this
    }

    fun withNaisStsSupport() : WireMockBuilder {
        val naisSts = NaisStsTokenResponseTransformer(NAIS_STS_TRANSFORMER, NAIS_STS_ISSUER)
        config.extensions(naisSts)
        withNaisStsSupport = true
        return this
    }

    private fun addLoginServiceStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching(".*${Paths.LOGIN_SERVICE_V1_LOGIN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(LOGIN_SERVICE_V1_TRANSFORMER)))
        WireMockStubs.stubJwks(Paths.LOGIN_SERVICE_V1_JWKS_PATH)
        WireMockStubs.stubWellKnown(
                path = Paths.LOGIN_SERVICE_V1_WELL_KNOWN_PATH,
                issuer = LoginService.V1_0.getIssuer(),
                jwkSetUrl = server.getLoginServiceV1JwksUrl(),
                tokenEndpoint = "http://localhost:8080/not-in-use-for-login-service"
        )
    }

    private fun addAzureStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching(".*${Paths.AZURE_V1_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(AZURE_V1_TRANSFORMER)))
        WireMockStubs.stubJwks(Paths.AZURE_V1_JWKS_PATH)
        WireMockStubs.stubWellKnown(
                path = Paths.AZURE_V1_WELL_KNOWN_PATH,
                issuer = Azure.V1_0.getIssuer(),
                jwkSetUrl = server.getAzureV1JwksUrl(),
                tokenEndpoint = server.getAzureV1TokenUrl()
        )

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching(".*${Paths.AZURE_V2_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(AZURE_V2_TRANSFORMER)))
        WireMockStubs.stubJwks(Paths.AZURE_V2_JWKS_PATH)
        WireMockStubs.stubWellKnown(
                path = Paths.AZURE_V2_WELL_KNOWN_PATH,
                issuer = Azure.V2_0.getIssuer(),
                jwkSetUrl = server.getAzureV2JwksUrl(),
                tokenEndpoint = server.getAzureV2TokenUrl()
        )
    }

    private fun addNaisStsStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching(".*${Paths.NAIS_STS_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(NAIS_STS_TRANSFORMER)))
        WireMockStubs.stubJwks(Paths.NAIS_STS_JWKS_PATH)
        WireMockStubs.stubWellKnown(
                path = Paths.NAIS_STS_WELL_KNOWN_PATH,
                issuer = NAIS_STS_ISSUER,
                jwkSetUrl = server.getNaisStsJwksUrl(),
                tokenEndpoint = server.getNaisStsTokenUrl()
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
        if (withLoginServieSupport) addLoginServiceStubs(server)
        if (withNaisStsSupport) addNaisStsStubs(server)

        return server
    }
}