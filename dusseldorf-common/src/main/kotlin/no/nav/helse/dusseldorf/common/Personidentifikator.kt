package no.nav.helse.dusseldorf.common

import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

/**
 * Klassen Personidentifikator respresenterer enten en FNR, DNR, TEST_NORGE_FNR eller TEST_NORGE_DNR.
 *
 * Validering skjer ved instansiering. Ved valideringsfeil kastes det en IllegalArgumentException.
 *
 * For mer info om fnr og dn
 * @see <a href="http://www.fnrinfo.no/Info/Oppbygging.aspx">fnrinfo.no</a>
 */
class Personidentifikator(private val ident: String) {

    private companion object {
        private val logger = LoggerFactory.getLogger(Personidentifikator::class.java)

        val identRegex = Regex("\\d{11}") // 11 siffer

        val TEST_NORGE_GRENSEVERDI = 8 //Syntetisk bruker fra TestNorge som har tredje siffer + 8
        val D_NUMMER_GRENSEVERDI = 4 //D-nummer har første siffer + 4
        val DOLLY_FNR_GRENSEVERDI = 40 //FNR fra Dolly har + 40 på måned

        val vekttallProviderFnr1: (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2).reversedArray()[it] }
        val vekttallProviderFnr2: (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2).reversedArray()[it] }

        val fødselsdatoFormat = DateTimeFormatter.ofPattern("ddMMyy")
    }

    val type: Type

    init {
        erElleveSiffer()
        type = when {
            erTestNorgeDNummer() -> Type.TEST_NORGE_DNR
            erTestNorgeFNummer() -> Type.TEST_NORGE_FNR
            erDollyDnr() -> Type.DOLLY_DNR
            erDollyFnr() -> Type.DOLLY_FNR
            erDNummer() -> Type.DNR
            else -> Type.FNR
        }
        erGyldig()

    }

    fun erGyldig() {
        erGyldigFødselsdato()
        erGyldigKontrollsiffeEn()
        erGyldigKontrollsifferTo()
    }

    private fun erGyldigKontrollsifferTo() {
        val forventetKontrollsifferTo = ident[10] // siffer nr. 11

        val kalkulertKontrollsifferTo = Mod11.kontrollsiffer(
            number = ident.take(10),
            vekttallProvider = vekttallProviderFnr2
        )

        require(kalkulertKontrollsifferTo == forventetKontrollsifferTo) {
            val grunn =
                "kalkulertKontrollsifferTo ($kalkulertKontrollsifferTo) er ikke lik forventetKontrollsifferTo ($forventetKontrollsifferTo)"
            logger.warn(grunn)
            grunn
        }
    }

    private fun erGyldigKontrollsiffeEn() {
        val forventetKontrollsifferEn = ident[9] // siffer nr. 10

        val kalkulertKontrollsifferEn = Mod11.kontrollsiffer(
            number = ident.take(9),
            vekttallProvider = vekttallProviderFnr1
        )

        require(kalkulertKontrollsifferEn == forventetKontrollsifferEn) {
            val grunn =
                "kalkulertKontrollsifferEn ($kalkulertKontrollsifferEn) er ikke lik forventetKontrollsifferEn ($forventetKontrollsifferEn)"
            logger.warn(grunn)
            grunn
        }
    }

    private fun erGyldigFødselsdato() {
        require(starterMedFodselsdato()) {
            val grunn = "Ident starter ikke med en gyldig fødselsdato: ${ident.take(6)}"
            logger.warn(grunn)
            grunn
        }
    }

    private fun erElleveSiffer() {
        require(ident.matches(identRegex)) {
            val grunn = "Forventet at personidentifikator kun var siffer, men var ${maskIdent()} (${ident.length})"
            logger.warn(grunn)
            grunn
        }
    }

    private fun starterMedFodselsdato(): Boolean {
        // Sjekker ikke hvilket århundre vi skal tolket yy som, kun at det er en gyldig dato.
        // F.eks blir 290990 parset til 2090-09-29, selv om 1990-09-29 var ønskelig.
        // Kunne sett på individsifre (Tre første av personnummer) for å tolke århundre,
        // men virker unødvendig komplekst og sårbart for ev. endringer i fødselsnummeret.
        return try {
            return when (type) {
                Type.TEST_NORGE_DNR -> {
                    fødselsdatoFormat.parse(
                        ident
                            .tilVanligFraDNummer()
                            .tilVanligFraTestNorgeNummer()
                            .take(6)
                    )
                    true
                }
                Type.TEST_NORGE_FNR -> {
                    fødselsdatoFormat.parse(ident.tilVanligFraTestNorgeNummer().take(6))
                    true
                }
                Type.DNR -> {
                    fødselsdatoFormat.parse(ident.tilVanligFraDNummer().take(6))
                    true
                }
                Type.FNR -> {
                    fødselsdatoFormat.parse(ident.take(6))
                    true
                }

                Type.DOLLY_FNR -> {
                    val tilVanligFraDollyFNR = ident.tilVanligFraDollyFNR()
                    fødselsdatoFormat.parse(tilVanligFraDollyFNR.take(6))
                    true
                }

                Type.DOLLY_DNR -> {
                    val tilVanligFraDollyDNR = ident.tilVanligFraDollyDNR()
                    fødselsdatoFormat.parse(tilVanligFraDollyDNR.take(6))
                    true
                }
            }
        } catch (cause: Throwable) {
            logger.warn("Feilet med å parse fødselsdato for type: $type. Grunn:", cause)
            false
        }
    }

    override fun toString(): String {
        return maskIdent()
    }

    private fun maskIdent(): String {
       return ident.take(6) + "******"
    }

    private fun førsteSiffer(): Int = ident.substring(0, 1).toInt()
    private fun tredjeSiffer(): Int = ident.substring(2, 3).toInt()
    private fun tredjeOgFjerdeSiffer(): Int = ident.substring(2, 4).toInt()

    private fun String.tilVanligFraDNummer(): String = replaceRange(0, 1, "${førsteSiffer() - D_NUMMER_GRENSEVERDI}")
    private fun String.tilVanligFraTestNorgeNummer(): String =
        replaceRange(2, 3, "${tredjeSiffer() - TEST_NORGE_GRENSEVERDI}")

    private fun String.tilVanligFraDollyFNR(): String {
        val dag = this.substring(0, 2)
        var måned = this.substring(2, 4).toInt()
        val år = this.substring(4, 6)
        val personNummer = this.substring(6)

        måned -= DOLLY_FNR_GRENSEVERDI
        val nyMåned = if (måned < 10) "0$måned" else måned.toString()

        return "$dag$nyMåned$år$personNummer"
    }
    private fun String.tilVanligFraDollyDNR(): String {
        var dag = this.substring(0, 2)
        var måned = this.substring(2, 4).toInt()
        val år = this.substring(4, 6)
        val personNummer = this.substring(6)

        måned -= DOLLY_FNR_GRENSEVERDI
        val nyDag = dag.replaceRange(0, 1, "${førsteSiffer() - D_NUMMER_GRENSEVERDI}")
        val nyMåned = if (måned < 10) "0$måned" else måned.toString()

        return "$nyDag$nyMåned$år$personNummer"
    }

    private fun erTestNorgeFNummer(): Boolean = tredjeSiffer() >= TEST_NORGE_GRENSEVERDI
    private fun erDollyFnr(): Boolean = tredjeOgFjerdeSiffer() >= DOLLY_FNR_GRENSEVERDI
    private fun erDollyDnr(): Boolean = erDollyFnr() && erDNummer()
    private fun erDNummer(): Boolean = førsteSiffer() >= D_NUMMER_GRENSEVERDI
    private fun erTestNorgeDNummer(): Boolean = erTestNorgeFNummer() && erDNummer()

    enum class Type {
        FNR, DNR, TEST_NORGE_FNR, TEST_NORGE_DNR, DOLLY_FNR, DOLLY_DNR
    }
}
