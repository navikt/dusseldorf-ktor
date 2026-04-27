package no.nav.helse.dusseldorf.common

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

internal class PersonidentifikatorTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123456x7890", // ikke er kun siffer
            "1234567891", // 10 siffer,
            "917755736",
            "29099012345",
            "011261702690",
            "89108773345", // D-nummer der første siffer er større enn 7
        ]
    )
    internal fun `gitt ugyldige identer, forvent feil`(ident: String) {
        assertThrows(IllegalArgumentException::class.java) { Personidentifikator(ident) }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            // FNR
            "24420167209", "18410162721", "17420373147", "24420167209", "18410162721", "17420373147", "24420167209",
            "18410162721", "17420373147", "24420167209", "18410162721", "17420373147", "24420167209", "18410162721",
            "17420373147", "24420167209", "18410162721", "17420373147", "24420167209", "18410162721", "17420373147",
            "24420167209", "18410162721", "17420373147", "24420167209", "18410162721", "17420373147", "24420167209",

            // DNR
            "53488838241", // D-nummer med +4 på første siffer

            // TEST_NORGE_FNR
            "10915596784", //TestNorge bruker med +8 på tredje siffer

            // TEST_NORGE_DNR
            "42921275204", // D-nummer fra TestNorge med +4 på første siffer og +8 på tredje

            // DOLLY_FNR (+40 i måned)
            "17912099997", "12429400544", "12505209719", // Mann
            "29822099635", "05440355678", "21483609245", // Kvinne

            // DOLLY_DNR (+4 på første siffer og +40 i måned)
            "55507608360", "52429405181", "52505209540", // Mann
            "69422056629", "45440356293", "61483601467" // Kvinne
        ]
    )
    internal fun `gitt gyldige identer, forvent ingen feil`(ident: String) {
        assertDoesNotThrow { Personidentifikator(ident) }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "24420167209", "18410162721", "17420373147", "24420167209", "18410162721", "17420373147", "24420167209",
            "18410162721", "17420373147", "24420167209", "18410162721", "17420373147", "24420167209", "18410162721",
            "17420373147", "24420167209", "18410162721", "17420373147", "24420167209", "18410162721", "17420373147",
            "24420167209", "18410162721", "17420373147", "24420167209", "18410162721", "17420373147", "24420167209",
        ]
    )
    internal fun `gitt identer, forvent at de er av typen FNR`(ident: String) {
        assertEquals(Personidentifikator.Type.FNR, Personidentifikator(ident).type)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "53488838241"
        ]
    )
    internal fun `gitt identer, forvent at de er av typen DNR`(ident: String) {
        assertEquals(Personidentifikator.Type.DNR, Personidentifikator(ident).type)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "10915596784"
        ]
    )
    internal fun `gitt identer, forvent at de er av typen TEST_NORGE_FNR`(ident: String) {
        assertEquals(Personidentifikator.Type.TEST_NORGE_FNR, Personidentifikator(ident).type)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "42921275204"
        ]
    )
    internal fun `gitt identer, forvent at de er av typen TEST_NORGE_DNR`(ident: String) {
        assertEquals(Personidentifikator.Type.TEST_NORGE_DNR, Personidentifikator(ident).type)
    }
}
