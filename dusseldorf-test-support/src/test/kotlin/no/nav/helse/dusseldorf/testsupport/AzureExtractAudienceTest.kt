package no.nav.helse.dusseldorf.testsupport

import no.nav.helse.dusseldorf.testsupport.http.extractAudience
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AzureExtractAudienceTest {

    @Test
    fun `Henter ut riktig audience fra scopes`() {
        assertFailsWith<NoSuchElementException> {
            emptySet<String>().extractAudience()
        }

        assertEquals("mega-test", setOf("mega-test/.default").extractAudience())
        assertEquals("mega-test", setOf("mega-test/.default", "mega-test-2/.default", "foo").extractAudience())
        assertEquals("mega-test", setOf("api://mega-test", "api://mega-test-2", "foo").extractAudience())
        assertEquals("mega-test", setOf("foo","api://mega-test/.default").extractAudience())
    }
}