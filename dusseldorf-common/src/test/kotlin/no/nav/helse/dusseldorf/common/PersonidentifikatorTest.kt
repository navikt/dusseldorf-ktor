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
            "22448370767", // Riktig modulus sjekk, men ikke dato i starten,
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
            "25052299988", "25052299716", "25052299554", "25052299392", "25052299120", "25052298922", "25052298760",
            "25052298337", "25052298175", "25052297977", "25052297705", "25052297543", "25052297381", "25052296911",
            "25052297896", "25052297624", "25052297462", "25052297039", "25052296830", "25052296679", "25052296407",
            "25052296245", "25052296083", "25052295885", "25052295613", "25052295451", "25052295028", "25052294668",

            // DNR
            "59108773345", // D-nummer med +4 på første siffer

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
            "25052299988", "25052299716", "25052299554", "25052299392", "25052299120", "25052298922", "25052298760",
            "25052298337", "25052298175", "25052297977", "25052297705", "25052297543", "25052297381", "25052296911",
            "25052297896", "25052297624", "25052297462", "25052297039", "25052296830", "25052296679", "25052296407",
            "25052296245", "25052296083", "25052295885", "25052295613", "25052295451", "25052295028", "25052294668",
        ]
    )
    internal fun `gitt identer, forvent at de er av typen FNR`(ident: String) {
        assertEquals(Personidentifikator.Type.FNR, Personidentifikator(ident).type)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "59108773345"
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
