package no.nav.helse.dusseldorf.ktor.core

import io.ktor.server.config.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.ApplicationConfigExt")
private val VaultPath = "/var/run/secrets/nais.io/vault"

internal class ApplicationConfigExt(
        private val config : ApplicationConfig
) {
    private fun kildeOgVerdiOrNull(key: String) : Pair<String, String>? {
        val fromApplicationConfig = config.propertyOrNull(key)?.getString()
        if (!fromApplicationConfig.isNullOrBlank()) return "ApplicationConfig" to fromApplicationConfig

        val fromEnv = System.getenv(key)
        if (!fromEnv.isNullOrBlank()) return "EnvironmentVariable" to fromEnv

        val fromProperty = System.getProperty(key)
        if (!fromProperty.isNullOrBlank()) return "SystemProperty" to fromProperty

        return null
    }

    private fun getString(key: String, secret: Boolean, optional: Boolean) : String? {
        val (kilde, verdi) = kildeOgVerdiOrNull(key) ?: return when (optional) {
            true -> null
            false -> throw IllegalArgumentException("$key må settes.")
        }

        logger.info("{}={} ($kilde)", key, if (secret) "***" else verdi)

        return verdi
    }

    internal fun getRequiredString(key: String, secret: Boolean = false) : String = getString(key, secret, false)!!
    internal fun getOptionalString(key: String, secret: Boolean = false) : String? = getString(key, secret, true)
    internal fun <T>getListFromCsv(csv: String, builder: (value: String) -> T) : List<T> = csv.replace(" ", "").split(",").map(builder)
}

fun ApplicationConfig.getRequiredString(key: String, secret: Boolean) : String = ApplicationConfigExt(this).getRequiredString(key, secret)

fun ApplicationConfig.getOptionalString(key: String, secret: Boolean) : String? = ApplicationConfigExt(this).getOptionalString(key, secret)

fun <T>ApplicationConfig.getRequiredList(key : String, secret: Boolean, builder: (value: String) -> T) : List<T> {
    val ext = ApplicationConfigExt(this)
    val csv= ext.getRequiredString(key, secret)
    return ext.getListFromCsv(csv = csv, builder = builder)
}

fun <T>ApplicationConfig.getOptionalList(key : String, secret: Boolean, builder: (value: String) -> T) : List<T> {
    val ext = ApplicationConfigExt(this)
    val csv = ext.getOptionalString(key, secret) ?: return emptyList()
    return ext.getListFromCsv(csv = csv, builder = builder)
}

fun ApplicationConfig.id() : String = ApplicationConfigExt(this).getRequiredString("ktor.application.id", secret = false)

fun ApplicationConfig.getRequiredSecret(key: String) : String {
    val secret = getOptionalString(key, true)
    return if (secret != null) secret
    else {
        val file = File("$VaultPath/$key")
        require(file.exists()) { "$key må settes." }
        val value = file.readText(Charsets.UTF_8)
        require(!value.isBlank()) { "$key må settes." }
        value
    }
}
