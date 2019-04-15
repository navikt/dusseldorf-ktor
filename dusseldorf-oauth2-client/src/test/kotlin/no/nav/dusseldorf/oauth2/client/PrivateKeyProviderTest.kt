package no.nav.dusseldorf.oauth2.client

import org.bouncycastle.jce.provider.BouncyCastleProvider
import kotlin.test.Test
import java.security.Security
import kotlin.test.assertEquals

class PrivateKeyProviderTest {

    @Test
    fun `Alle PrivateKeyProviders gir samme resultat med samme private key`() {
        Security.addProvider(BouncyCastleProvider()) // For at det skal være mulig å laste RSAPrivateKey fra PEM

        val fromPem = FromPrivateKeyPem(TestData.PRIVATE_KEY_PEM).getPrivateKey()
        val fromJwk = FromJwk(TestData.PRIVATE_KEY_JWK).getPrivateKey()
        val direct = DirectPrivateKey(fromPem).getPrivateKey()

        assertEquals(fromPem, fromJwk)
        assertEquals(fromPem, direct)
        assertEquals(fromJwk, direct)
    }
}