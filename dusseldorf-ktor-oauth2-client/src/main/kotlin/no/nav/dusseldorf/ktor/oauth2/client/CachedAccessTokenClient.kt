package no.nav.dusseldorf.ktor.oauth2.client

import java.time.LocalDateTime

class CachedAccessTokenClient(
        private val accessTokenClient: AccessTokenClient
) {
    private val cachedTokens = mutableMapOf<Set<String>, CachedToken>()

    // Cacher ikke onBehalfOf
    fun getAuthorizationHeader(
            scopes: Set<String>,
            onBehalfOf: String): String = accessTokenClient.getAccessToken(scopes, onBehalfOf).getAuthorizationHeader()

    fun getAuthorizationHeader(
            scopes: Set<String>): String {
        val cachedToken = if (cachedTokens.containsKey(scopes)) cachedTokens[scopes] else null

        return if (cachedToken == null || cachedToken.isExpired()) {
            cachedTokens.remove(scopes)
            val accessTokenResponse = accessTokenClient.getAccessToken(scopes)
            cachedTokens[scopes] = CachedToken(accessTokenResponse)
            accessTokenResponse.getAuthorizationHeader()
        } else {
            cachedToken.getAuthorizationHeader()
        }
    }
}
private class CachedToken(
        accessTokenResponse: AccessTokenResponse
) {
    private val expires = LocalDateTime.now().plusSeconds(accessTokenResponse.expiresIn).minusSeconds(10)
    private val authorizationHeader = accessTokenResponse.getAuthorizationHeader()
    fun isExpired() : Boolean = LocalDateTime.now().isAfter(expires)
    fun getAuthorizationHeader() : String = authorizationHeader
}
