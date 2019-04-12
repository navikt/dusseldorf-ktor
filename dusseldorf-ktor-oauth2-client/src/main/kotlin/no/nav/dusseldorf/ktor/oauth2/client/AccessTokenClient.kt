package no.nav.dusseldorf.ktor.oauth2.client

interface AccessTokenClient {
    fun getAccessToken(scopes: Set<String>, onBehalfOf: String) : AccessTokenResponse
    fun getAccessToken(scopes: Set<String>) : AccessTokenResponse
}

// https://tools.ietf.org/html/rfc6749#section-4.4.3 - A refresh token SHOULD NOT be included
data class AccessTokenResponse(
        private val accessToken : String,
        val expiresIn: Long,
        private val tokenType: String
) {
    fun getAuthorizationHeader() = "$tokenType $accessToken"
}