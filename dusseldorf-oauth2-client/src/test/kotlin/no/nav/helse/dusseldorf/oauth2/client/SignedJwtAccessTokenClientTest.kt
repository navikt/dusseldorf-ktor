package no.nav.helse.dusseldorf.oauth2.client

import com.nimbusds.jwt.SignedJWT
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.helse.dusseldorf.testsupport.jws.Tokendings
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2TokenUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getTokendingsTokenUrl
import java.net.URI
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SignedJwtAccessTokenClientTest {

    @Test
    @Ignore
    fun `Manuelt teste mot Azure Preprod`() {
        val clientId = "set-me"
        val certificateThumbprint = "set-me"
        val privateKeyJwk = """
            set-me
        """.trimIndent()
        val scopes = setOf<String>()

        val client = SignedJwtAccessTokenClient(
                clientId = clientId,
                keyIdProvider = FromCertificateHexThumbprint(certificateThumbprint),
                tokenEndpoint = TestData.AZURE_PREPROD_TOKEN_URL,
                privateKeyProvider = FromJwk(privateKeyJwk)
        )

        val accessToken = client.getAccessToken(scopes)

        val jwt = SignedJWT.parse(accessToken.accessToken)
        println(jwt.parsedString)
        println(jwt.header.toJSONObject())
        println(jwt.jwtClaimsSet.toJSONObject())
    }

    @Test
    fun `Hente access token`() {
        val mock = Oauth2ServerWireMock()
        val tokenUrl = mock.getTokenUrl()
        val clientId = "test-client-id"

        mock.stubGetTokenSignedJwtClientCredentials()

        val client = SignedJwtAccessTokenClient(
                clientId = clientId,
                keyIdProvider = FromCertificateHexThumbprint(TestData.CERTIFICATE_THUMBPRINT_SHA1_HEX),
                tokenEndpoint = tokenUrl,
                privateKeyProvider = FromJwk(TestData.PRIVATE_KEY_JWK)
        )

        val resp = client.getAccessToken(
                scopes = setOf("en-annen-client/.default")
        )

        assertNotNull(resp)
        mock.stop()
    }

    @Test
    fun `Hente Azure access token med test support`() {
        val wireMock = WireMockBuilder()
                .withAzureSupport()
                .build()

        val client = SignedJwtAccessTokenClient(
                clientId = "foo",
                keyIdProvider = FromCertificateHexThumbprint(TestData.CERTIFICATE_THUMBPRINT_SHA1_HEX),
                tokenEndpoint = URI(wireMock.getAzureV2TokenUrl()),
                privateKeyProvider = FromJwk(TestData.PRIVATE_KEY_JWK)
        )

        val response = client.getAccessToken(setOf("fooscope/.default"))

        assertNotNull(response)
        wireMock.stop()
    }

    @Test
    fun `Hente Tokendings access token med test support`() {
        val wireMock = WireMockBuilder()
            .withTokendingsSupport()
            .build()

        val client = SignedJwtAccessTokenClient(
            clientId = "min-test-app-a",
            keyIdProvider = FromCertificateHexThumbprint(TestData.CERTIFICATE_THUMBPRINT_SHA1_HEX),
            tokenEndpoint = URI(wireMock.getTokendingsTokenUrl()),
            privateKeyProvider = FromJwk(TestData.PRIVATE_KEY_JWK)
        )

        val onBehalfOf = LoginService.V1_0.generateJwt(
            fnr = "12345566"
        )

        val response = client.getAccessToken(setOf("min-test-app-b"), onBehalfOf)

        val claims = SignedJWT.parse(response.accessToken).jwtClaimsSet

        assertEquals("12345566", claims.subject)
        assertEquals(Tokendings.getIssuer(), claims.issuer)
        assertEquals("min-test-app-a", claims.getStringClaim("client_id"))
        assertEquals("min-test-app-b", claims.audience.firstOrNull())

        wireMock.stop()
    }
}