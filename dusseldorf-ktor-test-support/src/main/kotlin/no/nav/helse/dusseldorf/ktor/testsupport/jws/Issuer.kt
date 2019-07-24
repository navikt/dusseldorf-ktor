package no.nav.helse.dusseldorf.ktor.testsupport.jws

interface Issuer {
    fun getPublicJwk() : String
    fun getIssuer() : String
}