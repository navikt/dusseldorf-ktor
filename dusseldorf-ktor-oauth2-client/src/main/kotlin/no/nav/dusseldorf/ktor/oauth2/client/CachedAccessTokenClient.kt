package no.nav.dusseldorf.ktor.oauth2.client

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Expiry
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

class CachedAccessTokenClient(
        private val accessTokenClient: AccessTokenClient,
        expiryLeeway : Duration = Duration.ofSeconds(10),
        maxCachedClientAccessTokens : Long = 10,
        maxCachedOnBehalfOfAccessTokens : Long = 200
) {

    private val clientAccessTokens : Cache<Key, AccessTokenResponse> = Caffeine.newBuilder()
            .expireAfter(AccessTokenExpiry(expiryLeeway))
            .maximumSize(maxCachedClientAccessTokens)
            .build()

    private val onBehalfOfTokens: Cache<Key, AccessTokenResponse> = Caffeine.newBuilder()
            .expireAfter(AccessTokenExpiry(expiryLeeway))
            .maximumSize(maxCachedOnBehalfOfAccessTokens)
            .build()

    fun getAccessToken(
            scopes: Set<String>,
            onBehalfOf: String): AccessToken {
        val response = onBehalfOfTokens.get(Key(scopes, onBehalfOf)) {
            accessTokenClient.getAccessToken(scopes, onBehalfOf)
        }
        return AccessToken(token = response!!.accessToken, type = response.tokenType)
    }
    fun getAccessToken(
            scopes: Set<String>): AccessToken {
        val response = clientAccessTokens.get(Key(scopes)) {
            accessTokenClient.getAccessToken(scopes)
        }
        return AccessToken(token = response!!.accessToken, type = response.tokenType)
    }
}
data class AccessToken(
        val token : String,
        private val type: String
) {
    fun asAuthoriationHeader() : String = "$type $token"
}

private data class Key(
        val scopes: Set<String>,
        val onBehalfOf: String? = null
)

private class AccessTokenExpiry(
        private val expiryLeeway: Duration
) : Expiry<Key, AccessTokenResponse> {

    private fun getExpiryInNanos(response: AccessTokenResponse) : Long {
        return if (response.expiresIn <= expiryLeeway.toSeconds()) response.expiresIn else {
            Duration.ofSeconds(response.expiresIn).minusSeconds(expiryLeeway.toSeconds()).toNanos()
        }
    }
    override fun expireAfterUpdate(key: Key, response: AccessTokenResponse, currentTime: Long, currentDuration: Long): Long = getExpiryInNanos(response)
    override fun expireAfterCreate(key: Key, response: AccessTokenResponse, currentTime: Long): Long = getExpiryInNanos(response)
    override fun expireAfterRead(key: Key, response: AccessTokenResponse, currentTime: Long, currentDuration: Long): Long = currentDuration
}