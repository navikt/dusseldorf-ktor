package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.helse.dusseldorf.testsupport.jws.JwsFunctions

internal object WireMockStubs {
    private const val ISSUER = "issuer"
    private const val JWKS_URI = "jwks_uri"
    private const val TOKEN_ENDPOINT = "token_endpoint"

    internal fun stubWellKnown(
            path: String,
            issuer: String,
            jwkSetUrl: String,
            tokenEndpoint: String
    ) {
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*$path.*"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withStatus(200)
                                        .withBody("""
                                            {
                                                "$ISSUER": "$issuer",
                                                "$JWKS_URI": "$jwkSetUrl",
                                                "$TOKEN_ENDPOINT": "$tokenEndpoint",
                                                "subject_types_supported": [
                                                    "pairwise"
                                                ]
                                            }
                                        """.trimIndent())
                        )
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
                                        .withHeader("Content-Type", "application/json")
                                        .withStatus(200)
                                        .withBody(jwkSet)
                        )
        )
    }
}