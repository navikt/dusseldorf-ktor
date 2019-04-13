package no.nav.dusseldorf.ktor.oauth2.client

import kotlin.test.Test
import kotlin.test.assertEquals

class KeyIdProviderTest {
    @Test
    fun `Alle KeyIdProviders gir samme resultat med samme sertifkat`() {
        val expectedKid = TestData.CERTIFICATE_THUMBPRINT_SHA1_HEX_BASE64
        val hexThumbprint = TestData.CERTIFICATE_THUMBPRINT_SHA1_HEX

        val fromHexThumbprint = FromCertificateHexThumbprint(hexThumbprint).getKeyId()
        val direct = DirectKeyId(expectedKid).getKeyId()
        val fromPem = FromCertificatePem(TestData.CERTIFICATE_PEM).getKeyId()

        assertEquals(expectedKid, fromHexThumbprint)
        assertEquals(expectedKid, direct)
        assertEquals(expectedKid, fromPem)
    }
}