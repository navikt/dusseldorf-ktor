package no.nav.helse.dusseldorf.testsupport.jws

interface Issuer {
    fun getPublicJwk() : String
    fun getIssuer() : String
}