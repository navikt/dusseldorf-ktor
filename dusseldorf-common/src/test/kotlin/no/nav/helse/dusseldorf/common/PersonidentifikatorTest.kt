package no.nav.helse.dusseldorf.common

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

internal class PersonidentifikatorTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "123456x7890", // ikke er kun siffer
        "1234567891", // 10 siffer,
        "917755736",
        "29099012345",
        "011261702690",
        "22448370767" // Riktig modulus sjekk, men ikke dato i starten
    ])
    internal fun `gitt ugyldige identer, forvent feil`(ident: String) {
        assertThrows(IllegalArgumentException::class.java) { Personidentifikator(ident)}
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "01126170269",
        "13085973295",
        "25107774818",
        "19108773351",
        "10915596784", //TestNorge bruker med +8 på tredje siffer
        "42921275204", // D-nummer fra TestNorge med +4 på første siffer og +8 på tredje
        "59108773345", // D-nummer med +4 på første siffer
    ])
    internal fun `gitt gyldige identer, forvent ingen feil`(ident: String) {
        assertDoesNotThrow { Personidentifikator(ident) }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "01126170269",
        "13085973295",
        "25107774818",
        "19108773351"
    ])
    internal fun `gitt identer, forvent at de er av typen FNR`(ident: String) {
        assertEquals(Personidentifikator.Type.FNR, Personidentifikator(ident).type)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "59108773345"
    ])
    internal fun `gitt identer, forvent at de er av typen DNR`(ident: String) {
        assertEquals(Personidentifikator.Type.DNR, Personidentifikator(ident).type)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "10915596784"
    ])
    internal fun `gitt identer, forvent at de er av typen TEST_NORGE_FNR`(ident: String) {
        assertEquals(Personidentifikator.Type.TEST_NORGE_FNR, Personidentifikator(ident).type)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "42921275204"
    ])
    internal fun `gitt identer, forvent at de er av typen TEST_NORGE_DNR`(ident: String) {
        assertEquals(Personidentifikator.Type.TEST_NORGE_DNR, Personidentifikator(ident).type)
    }
}
