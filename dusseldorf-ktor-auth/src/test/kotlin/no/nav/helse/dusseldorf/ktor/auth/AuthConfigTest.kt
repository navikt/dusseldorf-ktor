package no.nav.helse.dusseldorf.ktor.auth

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalAPI
class AuthConfigTest {

    private companion object {
        private const val ISSUER_PREFIX = "nav.auth.issuers"
        private const val CLIENT_PREFIX = "nav.auth.clients"
        private val mock = OidcServerWiremock()
    }

    @Test
    fun `3 issuers konfigurert`() {
        val config = config(
                "$ISSUER_PREFIX.0.alias" to "nais-sts",
                "$ISSUER_PREFIX.0.issuer" to "http://localhost:8080",
                "$ISSUER_PREFIX.0.jwks_uri" to "http://loacalhost:8080/jwks",

                "$ISSUER_PREFIX.1.alias" to "azure-v1",
                "$ISSUER_PREFIX.1.issuer" to "http://localhost:8081",
                "$ISSUER_PREFIX.1.jwks_uri" to "http://loacalhost:8081/jwks",

                "$ISSUER_PREFIX.2.alias" to "azure-v2",
                "$ISSUER_PREFIX.2.issuer" to "http://localhost:8082",
                "$ISSUER_PREFIX.2.jwks_uri" to "http://loacalhost:8082/jwks"
        )
        val issuers = config.issuers()
        assertEquals(3, issuers.size)
    }

    @Test
    fun `Issuers ikke komplett konfigurert`() {
        val config = config(
                "$ISSUER_PREFIX.0.alias" to "nais-sts",
                "$ISSUER_PREFIX.0.issuer" to "http://localhost:8080",

                "$ISSUER_PREFIX.1.alias" to "azure-v1",
                "$ISSUER_PREFIX.1.jwks_uri" to "http://loacalhost:8081/jwks",

                "$ISSUER_PREFIX.2.alias" to "azure-v2"
        )
        val issuers = config.issuers()
        assertTrue(issuers.isEmpty())
    }

    @Test
    fun `1 client med client secret og en client med private key konfigurert`() {
        val config = config(
                "$CLIENT_PREFIX.0.alias" to "nais-sts",
                "$CLIENT_PREFIX.0.client_id" to "nais-sts-client-id",
                "$CLIENT_PREFIX.0.client_secret" to "very-very-secret",
                "$CLIENT_PREFIX.0.token_endpoint" to "http://loacalhost:8080/token",

                "$CLIENT_PREFIX.1.alias" to "azure",
                "$CLIENT_PREFIX.1.client_id" to "azure-client-id",
                "$CLIENT_PREFIX.1.private_key_jwk" to """{}""".trimIndent(),
                "$CLIENT_PREFIX.1.certificate_hex_thumbprint" to "aaa",
                "$CLIENT_PREFIX.1.token_endpoint" to "http://loacalhost:8081/token"
        )
        val clients = config.clients()
        assertEquals(2, clients.size)
    }

    @Test
    fun `Clients ikke komplett konfigurert`() {
        val config = config(
                "$CLIENT_PREFIX.0.alias" to "nais-sts",
                "$CLIENT_PREFIX.0.client_id" to "",

                "$CLIENT_PREFIX.1.alias" to "azure"
        )
        val clients = config.clients()
        assertTrue(clients.isEmpty())
    }

    @Test
    fun `Issuer som bruker discovery endpoint`() {
        val config = config(
                "$ISSUER_PREFIX.0.alias" to "nais-sts",
                "$ISSUER_PREFIX.0.discovery_endpoint" to mock.getValidDiscoveryEndpoint()
        )
        val issuers = config.issuers()
        assertEquals(1, issuers.size)
    }

    @Test
    fun `Private key client defaulter til å hente thumbprint fra JWK, men kan også konfigureres eksplisitt`() {
        val config = config(
                "$CLIENT_PREFIX.0.alias" to "azure-v1",
                "$CLIENT_PREFIX.0.client_id" to "azure-client-id",
                "$CLIENT_PREFIX.0.private_key_jwk" to """{}""".trimIndent(),
                "$CLIENT_PREFIX.0.certificate_hex_thumbprint" to "jeg-er-konfigurert-eksplisitt",
                "$CLIENT_PREFIX.0.discovery_endpoint" to mock.getValidDiscoveryEndpoint(),

                "$CLIENT_PREFIX.1.alias" to "azure-v2",
                "$CLIENT_PREFIX.1.client_id" to "azure-client-id",
                "$CLIENT_PREFIX.1.private_key_jwk" to """{"kid" : "jeg-er-hentet-fra-kid"}""".trimIndent(),
                "$CLIENT_PREFIX.1.discovery_endpoint" to mock.getValidDiscoveryEndpoint()
        )
        val client = config.clients()
        assertEquals(2, client.size)
        assertEquals("jeg-er-konfigurert-eksplisitt", (client["azure-v1"] as PrivateKeyClient).certificateHexThumbprint)
        assertEquals("jeg-er-hentet-fra-kid", (client["azure-v2"] as PrivateKeyClient).certificateHexThumbprint)
    }



    private fun config(vararg pairs: Pair<String, String>) = HoconApplicationConfig(ConfigFactory.parseMap(mapOf(*pairs)))
}