package no.nav.helse.dusseldorf.ktor.core

import org.junit.Test
import kotlin.test.assertEquals

class UrlTemplateTest {

    @Test
    fun `Test url med query parameters`(){
        assertEquals(
                "http://localhost.no/dokument?eier={eier}&navn={navn}",
                "http://localhost.no/dokument?eier=123&navn=noe".templateQueryParameters()
        )
    }

    @Test
    fun `Test url uten query parameters`(){
        assertEquals(
                "http://localhost.no/dokument",
                "http://localhost.no/dokument".templateQueryParameters()
        )
    }

}