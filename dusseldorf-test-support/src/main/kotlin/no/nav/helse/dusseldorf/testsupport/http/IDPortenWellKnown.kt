package no.nav.helse.dusseldorf.testsupport.http

object IDPortenWellKnown {
    /**
     * [https://oidc-ver2.difi.no/idporten-oidc-provider/.well-known/openid-configuration](https://oidc-ver2.difi.no/idporten-oidc-provider/.well-known/openid-configuration)
     */
    fun response(
            issuer : String,
            tokenEndpoint: String,
            jwksUri: String
    ) =
        //language=json
        """
        {
            "issuer": "$issuer",
            "authorization_endpoint": "https://oidc-ver2.difi.no/idporten-oidc-provider/authorize",
            "pushed_authorization_request_endpoint": "https://oidc-ver2.difi.no/idporten-oidc-provider/par",
            "token_endpoint": "$tokenEndpoint",
            "end_session_endpoint": "https://oidc-ver2.difi.no/idporten-oidc-provider/endsession",
            "revocation_endpoint": "https://oidc-ver2.difi.no/idporten-oidc-provider/revoke",
            "jwks_uri": "$jwksUri",
            "response_types_supported": [
                "code",
                "id_token",
                "id_token token",
                "token"
            ],
            "response_modes_supported": [
                "query",
                "form_post",
                "fragment"
            ],
            "subject_types_supported": [
                "pairwise"
            ],
            "id_token_signing_alg_values_supported": [
                "RS256"
            ],
            "code_challenge_methods_supported": [
                "S256"
            ],
            "userinfo_endpoint": "https://oidc-ver2.difi.no/idporten-oidc-provider/userinfo",
            "scopes_supported": [
                "openid",
                "profile"
            ],
            "ui_locales_supported": [
                "nb",
                "nn",
                "en",
                "se"
            ],
            "acr_values_supported": [
                "Level3",
                "Level4"
            ],
            "frontchannel_logout_supported": true,
            "frontchannel_logout_session_supported": true,
            "introspection_endpoint": "https://oidc-ver2.difi.no/idporten-oidc-provider/tokeninfo",
            "token_endpoint_auth_methods_supported": [
                "client_secret_post",
                "client_secret_basic",
                "private_key_jwt",
                "none"
            ],
            "request_parameter_supported": true,
            "request_uri_parameter_supported": false,
            "request_object_signing_alg_values_supported": [
                "RS256",
                "RS384",
                "RS512"
            ]
        }
    """.trimIndent()
}
