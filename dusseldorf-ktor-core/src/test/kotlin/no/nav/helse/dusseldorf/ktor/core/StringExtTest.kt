package no.nav.helse.dusseldorf.ktor.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

private val LOG: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.StringExtTest")

class StringExtTest {
    @Test
    fun `validering av organisasjonsnummer fungerer`() {
        val organisasjonsNummer = mapOf(
                "974652277" to true,
                "999263550" to true,
                "995199939" to true,
                "921858361" to true,
                "917755736" to true,
                "874652202" to true,
                "874652201" to false,
                "87465220F" to false,
                "974652277 " to false
        )

        organisasjonsNummer.forEach { orgnr, expectedResult ->
            LOG.info("Tester $orgnr -> $expectedResult")
            assertEquals(expectedResult, orgnr.erGyldigOrganisasjonsnummer())
        }
    }
}
