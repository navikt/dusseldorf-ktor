package no.nav.helse.dusseldorf.ktor.client

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class UrlExtKtTest {

    @Test
    fun `gitt url med path forvent riktig url når det legges til en ny path`() {
        val completeUrl = Url.buildURL(
            baseUrl = URI("http://localhost:8080"),
            pathParts = listOf("v1", "dokument")
        )

        println(completeUrl)

        val urlMedDokumentId = Url.buildURL(
            baseUrl = completeUrl,
            pathParts = listOf("123")
        ).toString()

        assertEquals("http://localhost:8080/v1/dokument/123", urlMedDokumentId)
    }
}
