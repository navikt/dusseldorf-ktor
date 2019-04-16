package no.nav.helse.dusseldorf.oauth2.client

interface AccessTokenClient {
    fun getAccessToken(scopes: Set<String>, onBehalfOf: String) : AccessTokenResponse
    fun getAccessToken(scopes: Set<String>) : AccessTokenResponse
}

// https://tools.ietf.org/html/rfc6749#section-4.4.3 - A refresh token SHOULD NOT be included
data class AccessTokenResponse(
        val accessToken : String,
        val expiresIn: Long,
        val tokenType: String
)