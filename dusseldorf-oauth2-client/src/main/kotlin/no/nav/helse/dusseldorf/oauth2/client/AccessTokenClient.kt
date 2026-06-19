package no.nav.helse.dusseldorf.oauth2.client

interface AccessTokenClient {
    fun getOnBehalfOfAccessToken(scopes: Set<String>, onBehalfOf: String) : AccessTokenResponse
    fun getClientCredentialsAccessToken(scopes: Set<String>) : AccessTokenResponse

    @Deprecated("Bruk getOnBehalfOfAccessToken eller getClientCredentialsAccessToken", level = DeprecationLevel.ERROR)
    fun getAccessToken(scopes: Set<String>, onBehalfOf: String) : AccessTokenResponse {
        return getOnBehalfOfAccessToken(scopes, onBehalfOf)
    }

    @Deprecated("Bruk getOnBehalfOfAccessToken eller getClientCredentialsAccessToken", level = DeprecationLevel.ERROR)
    fun getAccessToken(scopes: Set<String>) : AccessTokenResponse {
        return getClientCredentialsAccessToken(scopes)
    }
}

// https://tools.ietf.org/html/rfc6749#section-4.4.3 - A refresh token SHOULD NOT be included
data class AccessTokenResponse(
        val accessToken : String,
        val expiresIn: Long,
        val tokenType: String
)