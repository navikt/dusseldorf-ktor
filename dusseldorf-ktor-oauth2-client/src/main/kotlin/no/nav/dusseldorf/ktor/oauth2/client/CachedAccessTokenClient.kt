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

    fun getAuthorizationHeader(
            scopes: Set<String>,
            onBehalfOf: String): String {
        return onBehalfOfTokens.get(Key(scopes, onBehalfOf)) {
            accessTokenClient.getAccessToken(scopes, onBehalfOf)
        }!!.getAuthorizationHeader()
    }
    fun getAuthorizationHeader(
            scopes: Set<String>): String {
        return clientAccessTokens.get(Key(scopes)) {
            accessTokenClient.getAccessToken(scopes)
        }!!.getAuthorizationHeader()
    }
}

private data class Key(
        val scopes: Set<String>,
        val onBehalfOf: String? = null
)

private class AccessTokenExpiry(
        private val expiryLeeway: Duration
) : Expiry<Key, AccessTokenResponse> {

    private fun getExpiryInNanos(response: AccessTokenResponse, currentTime: Long) : Long =
        Duration.ofNanos(currentTime)
                .plusSeconds(response.expiresIn)
                .minusSeconds(expiryLeeway.toSeconds())
                .toNanos()

    override fun expireAfterUpdate(key: Key, response: AccessTokenResponse, currentTime: Long, currentDuration: Long): Long = getExpiryInNanos(response, currentTime)
    override fun expireAfterCreate(key: Key, response: AccessTokenResponse, currentTime: Long): Long = getExpiryInNanos(response, currentTime)
    override fun expireAfterRead(key: Key, response: AccessTokenResponse, currentTime: Long, currentDuration: Long): Long = currentDuration
}