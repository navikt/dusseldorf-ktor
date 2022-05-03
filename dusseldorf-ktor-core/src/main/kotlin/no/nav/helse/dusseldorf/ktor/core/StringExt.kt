package no.nav.helse.dusseldorf.ktor.core

import java.net.URL
import java.time.format.DateTimeFormatter

private val KUN_SIFFER = Regex("\\d+")

internal val vekttallProviderFnr1 : (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2).reversedArray()[it] }
internal val vekttallProviderFnr2 : (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2).reversedArray()[it] }
private val fnrDateFormat = DateTimeFormatter.ofPattern("ddMMyy")

fun String.fromResources() : URL = Thread.currentThread().contextClassLoader.getResource(this)

fun String.erKunSiffer() = matches(KUN_SIFFER)

private fun String.starterMedFodselsdato() : Boolean {
    // Sjekker ikke hvilket århundre vi skal tolket yy som, kun at det er en gyldig dato.
    // F.eks blir 290990 parset til 2090-09-29, selv om 1990-09-29 var ønskelig.
    // Kunne sett på individsifre (Tre første av personnummer) for å tolke århundre,
    // men virker unødvendig komplekst og sårbart for ev. endringer i fødselsnummeret.
    return try {
        val siffer3 = substring(2,3).toInt()
        if (siffer3 >= 8) { //Syntetisk bruker fra TestNorge som har tredje siffer + 8
            val fnr = this.replaceRange(2,3, "${siffer3-8}")
            fnrDateFormat.parse(fnr.substring(0, 6))
        } else fnrDateFormat.parse(substring(0, 6))
        true
    } catch (cause: Throwable) {
        false
    }
}

fun String.erGyldigFodselsnummer() : Boolean {
    if (length != 11 || !erKunSiffer() || !starterMedFodselsdato()) return false

    val forventetKontrollsifferEn = get(9)

    val kalkulertKontrollsifferEn = Mod11.kontrollsiffer(
            number = substring(0, 9),
            vekttallProvider = vekttallProviderFnr1
    )

    if (kalkulertKontrollsifferEn != forventetKontrollsifferEn) return false

    val forventetKontrollsifferTo = get(10)

    val kalkulertKontrollsifferTo = Mod11.kontrollsiffer(
            number = substring(0, 10),
            vekttallProvider = vekttallProviderFnr2
    )

    return kalkulertKontrollsifferTo == forventetKontrollsifferTo
}

fun String.erGyldigOrganisasjonsnummer() : Boolean {
    if (length != 9 || !erKunSiffer()) return false

    val kontrollsiffer = get(8)

    return Mod11.kontrollsiffer(substring(0,8)) == kontrollsiffer
}

/**
 * https://github.com/navikt/helse-sparkel/blob/2e79217ae00632efdd0d4e68655ada3d7938c4b6/src/main/kotlin/no/nav/helse/ws/organisasjon/Mod11.kt
 * https://www.miles.no/blogg/tema/teknisk/validering-av-norske-data
 */
internal object Mod11 {
    private val defaultVekttallProvider: (Int) -> Int = { 2 + it % 6 }

    internal fun kontrollsiffer(
            number: String,
            vekttallProvider : (Int) -> Int = defaultVekttallProvider
    ) : Char {
        return number.reversed().mapIndexed { i, char ->
            Character.getNumericValue(char) * vekttallProvider(i)
        }.sum().let(::kontrollsifferFraSum)
    }


    private fun kontrollsifferFraSum(sum: Int) = sum.rem(11).let { rest ->
        when (rest) {
            0 -> '0'
            1 -> '-'
            else -> "${11 - rest}"[0]
        }
    }
}