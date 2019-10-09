package no.nav.helse.dusseldorf.oauth2.client

import kotlin.test.Test
import kotlin.test.assertEquals

class PrivateKeyProviderTest {

    @Test
    fun `Alle PrivateKeyProviders gir samme resultat med samme private key`() {
        val fromPem = FromPrivateKeyPem(TestData.PRIVATE_KEY_PEM).getPrivateKey()
        val fromJwk = FromJwk(TestData.PRIVATE_KEY_JWK).getPrivateKey()
        val direct = DirectPrivateKey(fromPem).getPrivateKey()

        assertEquals(fromPem, fromJwk)
        assertEquals(fromPem, direct)
        assertEquals(fromJwk, direct)
    }
}