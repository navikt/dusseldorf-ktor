package no.nav.dusseldorf.ktor.oauth2.client

import kotlin.test.Test
import kotlin.test.assertEquals

class KeyIdProviderTest {
    @Test
    fun `Alle KeyIdProviders gir samme resultat med samme sertifkat`() {
        val expectedKid = "xJ03QEFFjRIQnh7ZpIecI9KOx_0="
        val hexThumbprint = "C49D374041458D12109E1ED9A4879C23D28EC7FD"

        val fromHexThumbprint = FromCertificateHexThumbprint(hexThumbprint).getKeyId()
        val direct = DirectKeyId(expectedKid).getKeyId()
        val fromPem = FromCertificatePem(TestData.CERTIFICATE_PEM).getKeyId()

        assertEquals(expectedKid, fromHexThumbprint)
        assertEquals(expectedKid, direct)
        assertEquals(expectedKid, fromPem)
    }
}