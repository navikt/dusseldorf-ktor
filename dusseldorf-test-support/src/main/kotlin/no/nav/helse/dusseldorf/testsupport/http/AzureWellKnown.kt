package no.nav.helse.dusseldorf.testsupport.http

object AzureWellKnown {
    fun response(
            issuer : String,
            tokenEndpoint: String,
            jwksUri: String,
            authorizationEndpoint: String
    ) = """
        {
            "issuer": "$issuer",
            "jwks_uri": "$jwksUri",
            "token_endpoint": "$tokenEndpoint",
            "authorization_endpoint": "$authorizationEndpoint",
            "response_modes_supported": [
              "form_post"
            ],
            "response_types_supported": [
              "code"
            ],
            "scopes_supported": [
              "openid",
              "profile"
            ],
            "subject_types_supported": [
                "pairwise"
            ]
        }
    """.trimIndent()
}