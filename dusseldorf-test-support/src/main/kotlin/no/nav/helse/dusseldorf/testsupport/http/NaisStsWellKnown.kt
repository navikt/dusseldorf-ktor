package no.nav.helse.dusseldorf.testsupport.http

object NaisStsWellKnown {
    fun response(
            issuer : String,
            tokenEndpoint: String,
            jwksUri: String
    ) = """
    {
        "issuer": "$issuer",
        "token_endpoint": "$tokenEndpoint",
        "jwks_uri": "$jwksUri",
        "subject_types_supported": [
            "public"
        ]
    }
    """.trimIndent()
}
