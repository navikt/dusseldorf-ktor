package no.nav.helse.dusseldorf.testsupport.http

object LoginServiceWellKnown {
    fun response(
            issuer : String,
            jwksUri: String
    ) = """
        {
            "issuer": "$issuer",
            "jwks_uri": "$jwksUri",
            "token_endpoint": "$issuer/not-in-use-for-login-service",
            "response_modes_supported": [
                "query",
                "fragment",
                "form_post"
            ],
            "response_types_supported": [
                "code",
                "code id_token",
                "code token",
                "code id_token token",
                "id_token",
                "id_token token",
                "token",
                "token id_token"
            ],
            "scopes_supported": [
              "openid"
            ],
            "subject_types_supported": [
                "pairwise"
            ]
        }
    """.trimIndent()
}