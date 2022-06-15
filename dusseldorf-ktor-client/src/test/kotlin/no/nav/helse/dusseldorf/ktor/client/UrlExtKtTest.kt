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

        val urlMedDokumentId = Url.buildURL(
            baseUrl = completeUrl,
            pathParts = listOf("123")
        ).toString()

        assertEquals("http://localhost:8080/v1/dokument/123", urlMedDokumentId)
    }
    
    @Test
    fun `lag forventet url med queryparameters`(){
        val baseUrl = Url.buildURL(
            baseUrl = URI("http://k9-joark")
        )

        val url = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("v1", "omsorgspengeutbetaling", "journalforing"),
            queryParameters = mapOf("parameter" to listOf("query1", "query2"))
        ).toString()

        assertEquals("http://k9-joark/v1/omsorgspengeutbetaling/journalforing?parameter=query1&parameter=query2", url)
    }
}
