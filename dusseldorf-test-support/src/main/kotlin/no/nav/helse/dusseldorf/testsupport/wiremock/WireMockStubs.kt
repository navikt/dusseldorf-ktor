package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

internal object WireMockStubs {
    private const val ISSUER = "issuer"
    private const val JWKS_URI = "jwks_uri"
    private const val TOKEN_ENDPOINT = "token_endpoint"

    internal fun stubWellKnown(
            path: String,
            response: String) {
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*$path.*"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                                        .withStatus(200)
                                        .withBody(response)
                        )
        )
    }

    internal fun stubWellKnown(
            path: String,
            issuer: String,
            jwkSetUrl: String,
            tokenEndpoint: String) {
        stubWellKnown(
                path = path,
                response = """
                    {
                        "$ISSUER": "$issuer",
                        "$JWKS_URI": "$jwkSetUrl",
                        "$TOKEN_ENDPOINT": "$tokenEndpoint"
                    }
                """.trimIndent()
        )
    }

    internal fun stubJwks(
            path: String,
            jwkSet: String
    ) {
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*$path.*"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                                        .withStatus(200)
                                        .withBody(jwkSet)
                        )
        )
    }
}