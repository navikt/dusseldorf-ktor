package no.nav.helse.dusseldorf.oauth2.client

import com.nimbusds.jwt.SignedJWT
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull

class ClientSecretAccessTokenClientTest {

    @Test
    @Ignore
    fun `Manuelt teste mot Azure Preprod`() {
        val clientId = "set-me"
        val clientSecret = "set-me"
        val scopes = setOf<String>()

        val client = ClientSecretAccessTokenClient(
                clientId = clientId,
                clientSecret = clientSecret,
                tokenEndpoint = TestData.AZURE_PREPROD_TOKEN_URL
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
        val clientSecret = "client-secret"

        mock.stubGetTokenClientSecretClientCredentials(clientId, clientSecret)

        val client = ClientSecretAccessTokenClient(
                clientId = clientId,
                clientSecret = clientSecret,
                tokenEndpoint = tokenUrl
        )

        val resp = client.getAccessToken(
                scopes = setOf("en-annen-client/.default")
        )

        assertNotNull(resp)
    }
}