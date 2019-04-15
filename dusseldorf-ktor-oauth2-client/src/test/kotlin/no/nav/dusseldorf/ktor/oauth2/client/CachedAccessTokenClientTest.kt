package no.nav.dusseldorf.ktor.oauth2.client

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class CachedAccessTokenClientTest {

    @Test
    fun `Test at cache fungerer som forventet`() {
        val mock = Oauth2ServerWireMock()
        val tokenUrl = mock.getTokenUrl()
        val clientId = "test-client"
        val clientSecret = "test-secret"
        val scopes = setOf("test-scope")
        val leewayInSeconds: Long = 10

        val client = ClientSecretAccessTokenClient(
                clientId = clientId,
                clientSecret = clientSecret,
                tokenUrl = tokenUrl
        )

        // Lager en client som forkaster token 10 sekunder før de utløper
        val cachedClient = CachedAccessTokenClient(accessTokenClient = client, expiryLeeway = Duration.ofSeconds(leewayInSeconds))

        // Mocker en response med token som varer i 11 sekunder
        mock.stubGetTokenClientSecretClientCredentials(clientId, clientSecret, expiresIn = leewayInSeconds + 1, accessToken = "Token1")

        // Henter første token
        assertEquals("Token1", cachedClient.getAccessToken(scopes).token)

        // Venter slikat tokene fortsatt bør være cached
        Thread.sleep(500)
        assertEquals(cachedClient.getAccessToken(scopes).token, "Token1")

        // Venter slik at tokenet bør være bort fra cache
        Thread.sleep(700)
        // Mocker ny response med nytt token
        mock.stubGetTokenClientSecretClientCredentials(clientId, clientSecret, expiresIn = leewayInSeconds + 1, accessToken = "Token2")
        assertEquals("Token2",cachedClient.getAccessToken(scopes).token)
    }
}