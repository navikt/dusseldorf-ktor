package no.nav.helse.dusseldorf.ktor.core

import java.net.URL

private val KUN_SIFFER = Regex("\\d+")

private val vekttallProviderFnr1 : (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2)[it] }
private val vekttallProviderFnr2 : (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)[it] }

fun String.fromResources() : URL = Thread.currentThread().contextClassLoader.getResource(this)

fun String.erKunSiffer() = matches(KUN_SIFFER)

fun String.erGyldigFodselsnummer() : Boolean {
    if (length != 11 || !erKunSiffer()) return false

    val forventetKontrollsifferEn = get(9)

    val kalkuleterKontrollSifferEn = Mod11.kontrollsiffer(
            number = substring(0, 9),
            vekttallProvider = vekttallProviderFnr1,
            reverse = false
    )

    if (kalkuleterKontrollSifferEn != forventetKontrollsifferEn) return false

    val forventetKontrollsifferTo = get(10)

    val kalkuleterKontrollSifferTo = Mod11.kontrollsiffer(
            number = substring(0, 10),
            vekttallProvider = vekttallProviderFnr2,
            reverse = false
    )

    return kalkuleterKontrollSifferTo == forventetKontrollsifferTo
}

fun String.erGyldigOrganisasjonsnummer() : Boolean {
    if (length != 9 || !erKunSiffer()) return false

    val kontrollSiffer = get(8)

    return Mod11.kontrollsiffer(substring(0,8)) == kontrollSiffer
}

/**
 * https://github.com/navikt/helse-sparkel/blob/2e79217ae00632efdd0d4e68655ada3d7938c4b6/src/main/kotlin/no/nav/helse/ws/organisasjon/Mod11.kt
 * https://www.miles.no/blogg/tema/teknisk/validering-av-norske-data
 */
private object Mod11 {
    private val defaultVekttallProvider: (Int) -> Int = { 2 + it % 6 }

    internal fun kontrollsiffer(
            number: String,
            vekttallProvider : (Int) -> Int = defaultVekttallProvider,
            reverse : Boolean = true
    ) : Char {
        val usedNumber = if (reverse) number.reversed() else number
        return usedNumber.mapIndexed { i, char ->
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