package no.nav.helse.dusseldorf.testsupport.http

object TokendingsWellKnown {
    fun response(
        issuer : String,
        tokenEndpoint: String,
        jwksUri: String
    ) = """
    {
        "issuer": "$issuer",
        "token_endpoint": "$tokenEndpoint",
        "jwks_uri": "$jwksUri"
    }
    """.trimIndent()
}