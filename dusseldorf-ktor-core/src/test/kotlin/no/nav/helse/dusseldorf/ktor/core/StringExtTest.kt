package no.nav.helse.dusseldorf.ktor.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random
import kotlin.test.Ignore
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

    // De gyldige fødselsnummerne er genrert fra funksjonen `genererFodselsnummer` nedenfor
    @Test
    fun `validering av fodselsnummer fungerer`() {
        val fodselsnummer = mapOf(
                "01126170269" to true,
                "13085973295" to true,
                "25107774818" to true,
                "19108773351" to true,
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

    @Test
    @Ignore
    fun `generer 4 tilfeldige fodselsnummer` () {
        for (i in 1..4) {
            var fnr : String? = null
            while (fnr == null) {
                fnr = genererFodselsnummer()
                if (fnr.contains("-")) fnr = null
            }
            if (!fnr.erGyldigFodselsnummer()) throw IllegalStateException("$fnr er ikke et gyldig fødselsnummer...")
            println(fnr)
        }
    }

    private fun genererFodselsnummer() : String{
        val dag = Random.nextInt(1,29).toString().padStart(2,'0')
        val maaned = Random.nextInt(1, 13).toString().padStart(2,'0')
        val aar = Random.nextInt(54, 100).toString()
        val individSiffer = Random.nextInt(700, 750).toString()
        val utenKontrollSiffer = "$dag$maaned$aar$individSiffer"
        val medForsteKontrollsiffer = utenKontrollSiffer + Mod11.kontrollsiffer(number = utenKontrollSiffer, vekttallProvider = vekttallProviderFnr1)
        return medForsteKontrollsiffer + Mod11.kontrollsiffer(number = medForsteKontrollsiffer, vekttallProvider = vekttallProviderFnr2)
    }
}
