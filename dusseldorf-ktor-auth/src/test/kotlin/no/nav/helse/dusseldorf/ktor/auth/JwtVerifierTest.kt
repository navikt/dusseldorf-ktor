package no.nav.helse.dusseldorf.ktor.auth

import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.jws.IDPorten
import no.nav.helse.dusseldorf.testsupport.jws.toDate
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2JwksUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getIDPortenJwksUrl
import org.junit.AfterClass
import org.junit.Test
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class JwtVerifierTest {

    @Test
    fun `Ugyldig jwt`() {
        assertFalse(azureJwtVerifier.verify("eyFoo.bar"))
    }

    @Test
    fun `Feil issuer`() {
        assertFalse(
            azureJwtVerifier.verify(
                Azure.V1_0.generateJwt(
                    clientId = "foo",
                    audience = "bar"
                )
            )
        )
    }

    @Test
    fun `Expired token`() {
        assertFalse(
            azureJwtVerifier.verify(
                Azure.V2_0.generateJwt(
                    clientId = "foo",
                    audience = "bar",
                    overridingClaims = mapOf(
                        "exp" to "${Instant.now().minusSeconds(60).epochSecond}"
                    )
                )
            )
        )
    }

    @Test
    fun `Manglende claims`() {
        assertFalse(
            azureJwtVerifier.verify(
                Azure.V2_0.generateJwt(
                    clientId = "foo",
                    audience = "bar"
                )
            )
        )
    }

    @Test
    fun `Token med riktig claims`() {
        assertTrue(
            azureJwtVerifier.verify(
                Azure.V2_0.generateJwt(
                    clientId = "foo",
                    audience = "bar",
                    overridingClaims = mapOf(
                        "tokenName" to "id_token"
                    )
                )
            )
        )
    }

    @Test
    fun `IDPorten idToken genereres som forventet`() {
        assertTrue(
            idPortenJwtVerifier.verify(
                IDPorten.generateIdToken(
                    fnr = "12345678910"
                )
            )
        )
    }

    @Test
    fun `Utgått IDPorten idToken feiler`() {
        assertFalse(
            idPortenJwtVerifier.verify(
                IDPorten.generateIdToken(
                    fnr = "12345678910",
                    expiration = LocalDateTime.now().minusSeconds(1).toDate()
                )
            )
        )
    }

    @Test
    fun `IDPorten accessToken genereres som forventet`() {
        assertTrue(
            idPortenJwtVerifier.verify(
                IDPorten.generateAccessToken(
                    fnr = "12345678910"
                )
            )
        )
    }

    @Test
    fun `Utgått IDPorten accessToken feiler`() {
        assertFalse(
            idPortenJwtVerifier.verify(
                IDPorten.generateAccessToken(
                    fnr = "12345678910",
                    expiration = LocalDateTime.now().minusSeconds(1).toDate()
                )
            )
        )
    }

    internal companion object {
        private val wireMock = WireMockBuilder()
            .withIDPortenSupport()
            .withAzureSupport().build()

        private val azureJwtVerifier = JwtVerifier(
            issuer = Issuer(
                alias = "azure-v2",
                issuer = Azure.V2_0.getIssuer(),
                jwksUri = URI(wireMock.getAzureV2JwksUrl()),
                audience = null
            ),
            additionalClaimRules = setOf(
                EnforceEqualsOrContains(
                    defaultClaimName = "tokenName",
                    expected = "id_token"
                )
            )
        )

        private val idPortenJwtVerifier = JwtVerifier(
            issuer = Issuer(
                alias = "id-porten",
                issuer = IDPorten.getIssuer(),
                jwksUri = URI(wireMock.getIDPortenJwksUrl())
            )
        )

        @AfterClass
        @JvmStatic
        fun cleanup() {
            wireMock.stop()
        }
    }
}
