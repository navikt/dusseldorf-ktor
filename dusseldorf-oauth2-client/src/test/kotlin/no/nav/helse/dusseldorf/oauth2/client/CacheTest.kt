package no.nav.helse.dusseldorf.oauth2.client

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CacheTest {

    @Test
    fun `Teste cach med expiry og maxsize`() {
        val cache: Cache<Int, Int> =
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(1))
                .maximumSize(10)
                .build()

        for (i in 1..10) { cache.put(i,i) }
        for (i in 1..10) { assertEquals(i, cache.getIfPresent(i))}
        Thread.sleep(1001)
        for (i in 1..10) { assertNull(cache.getIfPresent(i)) }
    }
}