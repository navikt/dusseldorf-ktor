package no.nav.helse.dusseldorf.ktor.build

import java.io.InputStream
import java.util.*

class DusseldorfKtorBuildProperties {

    companion object {
        private val properties : Properties = {
            val properties = Properties()
            properties.load("dusseldorf-ktor.properties".fromResources())
            properties
        }.invoke()

        fun get(key: String) : String {
            if (!properties.containsKey(key)) {
                throw IllegalArgumentException("Finnes ingen verdi for '$key'")
            }
            return properties.getProperty(key)
        }
    }
}

private fun String.fromResources() : InputStream = Thread.currentThread().contextClassLoader.getResource(this).openStream()