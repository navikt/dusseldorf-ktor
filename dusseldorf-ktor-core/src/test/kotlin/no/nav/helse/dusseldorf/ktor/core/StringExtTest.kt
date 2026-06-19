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

    // De gyldige fødselsnummerne fra https://github.com/navikt/sif-gha-workflows/blob/main/.github/actions/sif-code-scan/allowed-fnr/fnr.txt

    // TODO legg til D-nr i test når vi får et som er klarert for test|
    @Test
    fun `validering av fodselsnummer fungerer`() {
        val fodselsnummer = mapOf(
            "01015450572" to true,
            "01010000382" to true,
            "01017100552" to true,
            "10915596784" to true, //TestNorge bruker med +8 på tredje siffer
            "42921275204" to true, // D-nummer fra TestNorge med +4 på første siffer og +8 på tredje.
            "917755736" to false,
            "29099012345" to false,
            "011261702690" to false,
            "22448370767" to false // Riktig modulus sjekk, men ikke dato i starten
        )
        fodselsnummer.forEach { fnr, expectedResult ->
            LOG.info("Tester $fnr -> $expectedResult")
            assertEquals(expectedResult, fnr.erGyldigFodselsnummer())
        }
    }

}
