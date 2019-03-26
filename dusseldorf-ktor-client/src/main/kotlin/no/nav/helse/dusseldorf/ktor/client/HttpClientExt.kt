package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.slf4j.LoggerFactory
import java.net.ProxySelector

fun HttpAsyncClientBuilder.setProxyRoutePlanner() {
    setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
}
fun Logging.Config.sl4jLogger(
        name : String
) {
    logger = Sl4jLogger("no.nav.helse.dusseldorf.ktor.client.$name")
}

private class Sl4jLogger(
        name : String
) : Logger {
    private val sl4j = LoggerFactory.getLogger(name)
    override fun log(message: String) {
        sl4j.trace(message)
    }
}