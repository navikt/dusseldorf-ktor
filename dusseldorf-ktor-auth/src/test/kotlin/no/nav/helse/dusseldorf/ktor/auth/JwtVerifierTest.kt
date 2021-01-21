package no.nav.helse.dusseldorf.ktor.auth

import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2JwksUrl
import org.junit.AfterClass
import org.junit.Test
import java.net.URI
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class JwtVerifierTest {

    @Test
    fun `Ugyldig jwt`() {
        assertFalse(jwtVerifier.verify("eyFoo.bar"))
    }

    @Test
    fun `Feil issuer`() {
        assertFalse(jwtVerifier.verify(Azure.V1_0.generateJwt(
            clientId = "foo",
            audience = "bar"
        )))
    }

    @Test
    fun `Expired token`() {
        assertFalse(jwtVerifier.verify(Azure.V2_0.generateJwt(
            clientId = "foo",
            audience = "bar",
            overridingClaims = mapOf(
                "exp" to "${Instant.now().minusSeconds(60).epochSecond}"
            )
        )))
    }

    @Test
    fun `Manglende claims`() {
        assertFalse(jwtVerifier.verify(Azure.V2_0.generateJwt(
            clientId = "foo",
            audience = "bar"
        )))
    }

    @Test
    fun `Token med riktig claims`() {
        assertTrue(jwtVerifier.verify(Azure.V2_0.generateJwt(
            clientId = "foo",
            audience = "bar",
            overridingClaims = mapOf(
                "tokenName" to "id_token"
            )
        )))
    }

    internal companion object {
        private val wireMock = WireMockBuilder().withAzureSupport().build()

        private val jwtVerifier = JwtVerifier(
            issuer = Issuer(
                alias = "azure-v2",
                issuer = Azure.V2_0.getIssuer(),
                jwksUri = URI(wireMock.getAzureV2JwksUrl()),
                audience = null
            ),
            additionalClaimRules = setOf(EnforceEqualsOrContains(
                defaultClaimName = "tokenName",
                expected = "id_token"
            ))
        )

        @AfterClass @JvmStatic
        fun cleanup() {
            wireMock.stop()
        }
    }
}