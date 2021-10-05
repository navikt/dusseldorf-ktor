package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.helse.dusseldorf.testsupport.http.AzureWellKnown
import no.nav.helse.dusseldorf.testsupport.http.LoginServiceWellKnown
import no.nav.helse.dusseldorf.testsupport.http.NaisStsWellKnown
import no.nav.helse.dusseldorf.testsupport.http.TokendingsWellKnown
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.helse.dusseldorf.testsupport.jws.NaisSts
import no.nav.helse.dusseldorf.testsupport.jws.Tokendings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WireMockBuilder {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(WireMockBuilder::class.java)

        private const val AZURE_V1_TOKEN_TRANSFORMER = "azure-v1-token"
        private const val AZURE_V2_TOKEN_TRANSFORMER = "azure-v2-token"
        private const val LOGIN_SERVICE_V1_TRANSFORMER = "login-service-v1"
        private const val NAIS_STS_TRANSFORMER = "nais-sts"
        private const val TOKENDINGS_TRANSFORMER = "tokendings"
    }

    private val config = WireMockConfiguration.options()
    private var port: Int? = null
    private var serverFunction : ((wireMockServer: WireMockServer) -> Unit)? = null
    private var configFunction : ((wireMockConfiguration: WireMockConfiguration) -> Unit)? = null
    private var withAzureSupport = false
    private var withLoginServieSupport = false
    private var withNaisStsSupport = false
    private var withTokendingsSupport = false

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
        val azureV1 = AzureTokenResponseTransformer(name = AZURE_V1_TOKEN_TRANSFORMER, issuer = Azure.V1_0.getIssuer())
        val azureV2 = AzureTokenResponseTransformer(name = AZURE_V2_TOKEN_TRANSFORMER, issuer = Azure.V2_0.getIssuer())
        config.extensions(azureV1, azureV2)
        withAzureSupport = true
        return this
    }

    fun withLoginServiceSupport() : WireMockBuilder {
        val loginServiceV1 = LoginServiceLoginResponseTransformer(name = LOGIN_SERVICE_V1_TRANSFORMER)
        config.extensions(loginServiceV1)
        withLoginServieSupport = true
        return this
    }

    fun withNaisStsSupport() : WireMockBuilder {
        val naisSts = NaisStsTokenResponseTransformer(name = NAIS_STS_TRANSFORMER, issuer = NaisSts.getIssuer())
        config.extensions(naisSts)
        withNaisStsSupport = true
        return this
    }

    fun withTokendingsSupport() : WireMockBuilder {
        val tokedings = TokendingsResponseTransformer(name = TOKENDINGS_TRANSFORMER, issuer = Tokendings.getIssuer())
        config.extensions(tokedings)
        withTokendingsSupport = true
        return this
    }

    private fun addLoginServiceStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching(".*${Paths.LOGIN_SERVICE_V1_LOGIN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(LOGIN_SERVICE_V1_TRANSFORMER)))
        WireMockStubs.stubJwks(path = Paths.LOGIN_SERVICE_V1_JWKS_PATH, jwkSet = LoginService.V1_0.getPublicJwk())
        WireMockStubs.stubWellKnown(
                path = Paths.LOGIN_SERVICE_V1_WELL_KNOWN_PATH,
                response = LoginServiceWellKnown.response(
                        issuer = LoginService.V1_0.getIssuer(),
                        jwksUri = server.getLoginServiceV1JwksUrl()
                )
        )

        logger.info("Login Service V1 JWKS URL = ${server.getLoginServiceV1JwksUrl()}")
        logger.info("Login Service V1 Well-Known URL = ${server.getLoginServiceV1WellKnownUrl()}")
        logger.info("Login Service V1 Login URL = ${server.baseUrl()}${Paths.LOGIN_SERVICE_V1_LOGIN_PATH}?redirect={REDIRECT_URL}&fnr={FNR}")
    }

    private fun addAzureStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.post(WireMock
                .urlPathMatching(".*${Paths.AZURE_V1_TOKEN_PATH}.*"))
                .willReturn(WireMock.aResponse().withTransformers(AZURE_V1_TOKEN_TRANSFORMER)))

        WireMockStubs.stubJwks(path = Paths.AZURE_V1_JWKS_PATH, jwkSet = Azure.V1_0.getPublicJwk())
        WireMockStubs.stubWellKnown(
                path = Paths.AZURE_V1_WELL_KNOWN_PATH,
                response = AzureWellKnown.response(
                        issuer = Azure.V1_0.getIssuer(),
                        jwksUri = server.getAzureV1JwksUrl(),
                        tokenEndpoint = server.getAzureV1TokenUrl(),
                        authorizationEndpoint = server.getAzureV1AuthorizationUrl()
                )
        )

        logger.info("Azure V1 Token URL = ${server.getAzureV1TokenUrl()}")
        logger.info("Azure V1 JWKS URL = ${server.getAzureV1JwksUrl()}")
        logger.info("Azure V1 Well-Known URL = ${server.getAzureV1WellKnownUrl()}")

        WireMock.stubFor(WireMock.post(WireMock
                .urlPathMatching(".*${Paths.AZURE_V2_TOKEN_PATH}.*"))
                .willReturn(WireMock.aResponse().withTransformers(AZURE_V2_TOKEN_TRANSFORMER)))

        WireMockStubs.stubJwks(path = Paths.AZURE_V2_JWKS_PATH, jwkSet = Azure.V2_0.getPublicJwk())
        WireMockStubs.stubWellKnown(
                path = Paths.AZURE_V2_WELL_KNOWN_PATH,
                response = AzureWellKnown.response(
                        issuer = Azure.V2_0.getIssuer(),
                        jwksUri = server.getAzureV2JwksUrl(),
                        tokenEndpoint = server.getAzureV2TokenUrl(),
                        authorizationEndpoint = server.getAzureV2AuthorizationUrl()
                )
        )

        logger.info("Azure V2 Token URL = ${server.getAzureV2TokenUrl()}")
        logger.info("Azure V2 JWKS URL = ${server.getAzureV2JwksUrl()}")
        logger.info("Azure V2 Well-Known URL = ${server.getAzureV2WellKnownUrl()}")
    }

    private fun addNaisStsStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching(".*${Paths.NAIS_STS_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(NAIS_STS_TRANSFORMER)))
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching(".*${Paths.NAIS_STS_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(NAIS_STS_TRANSFORMER)))
        WireMockStubs.stubJwks(path = Paths.NAIS_STS_JWKS_PATH, jwkSet = NaisSts.getPublicJwk())
        WireMockStubs.stubWellKnown(
                path = Paths.NAIS_STS_WELL_KNOWN_PATH,
                response = NaisStsWellKnown.response(
                        issuer = NaisSts.getIssuer(),
                        jwksUri = server.getNaisStsJwksUrl(),
                        tokenEndpoint = server.getNaisStsTokenUrl()
                )
        )
        logger.info("Nais STS Token URL = ${server.getNaisStsTokenUrl()}")
        logger.info("Nais STS JWKS URL = ${server.getNaisStsJwksUrl()}")
        logger.info("Nais STS Well-Known URL = ${server.getNaisStsWellKnownUrl()}")
    }

    private fun addTokendingsStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching(".*${Paths.TOKENDINGS_TOKEN_PATH}.*")).willReturn(WireMock.aResponse().withTransformers(TOKENDINGS_TRANSFORMER)))
        WireMockStubs.stubJwks(path = Paths.TOKENDINGS_JWKS_PATH, jwkSet = Tokendings.getPublicJwk())
        WireMockStubs.stubWellKnown(
            path = Paths.TOKENDINGS_WELL_KNOWN_PATH,
            response = TokendingsWellKnown.response(
                issuer = Tokendings.getIssuer(),
                jwksUri = server.getTokendingsJwksUrl(),
                tokenEndpoint = server.getTokendingsTokenUrl()
            )
        )
        logger.info("Tokendings Token URL = ${server.getTokendingsTokenUrl()}")
        logger.info("Tokendings JWKS URL = ${server.getTokendingsJwksUrl()}")
        logger.info("Tokendings Well-Known URL = ${server.getTokendingsWellKnownUrl()}")
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
        if (withTokendingsSupport) addTokendingsStubs(server)

        logger.info("WireMock Base URL = ${server.baseUrl()}")

        return server
    }
}