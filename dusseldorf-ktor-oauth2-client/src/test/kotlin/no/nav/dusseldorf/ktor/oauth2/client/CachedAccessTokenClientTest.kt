package no.nav.dusseldorf.ktor.oauth2.client

import com.nimbusds.jwt.SignedJWT
import java.net.URL
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class CachedAccessTokenClientTest {
    @Test
    @Ignore
    fun `Får samme authorization header om det hentes ut rett etter hverandre med samme scopes`() {
        val client = SignedJwtAccessTokenClient(
                clientId = "4bd971d8-2469-434f-9322-8cfe7a7a3379",
                keyIdProvider = FromCertificateHexThumbprint(TestData.CERTIFICATE_THUMBPRINT_SHA1_HEX),
                tokenUrl = URL("https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/oauth2/v2.0/token"),
                privateKeyProvider = FromJwk(TestData.PRIVATE_KEY_JWK)
        )

        val scopes = setOf("5a5878bf-7654-490d-bbdd-6eb66caac4a3/.default")

        val cachedClient = CachedAccessTokenClient(client)

        val accessToken1 = cachedClient.getAccessToken(scopes)
        val accessToken2 = cachedClient.getAccessToken(scopes)

        assertEquals(accessToken1, accessToken2)

        val jwt = SignedJWT.parse(accessToken1.token)
        println(jwt.parsedString)
        println(jwt.header.toJSONObject())
        println(jwt.jwtClaimsSet.toJSONObject())
    }
}