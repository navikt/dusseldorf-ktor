package no.nav.helse.dusseldorf.ktor.core

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.ApplicationConfigExt")

@KtorExperimentalAPI
internal class ApplicationConfigExt(
        private val config : ApplicationConfig
) {
    private fun getString(key: String,
                          secret: Boolean,
                          optional: Boolean) : String? {
        val configValue = config.propertyOrNull(key) ?: return if (optional) null else throw IllegalArgumentException("$key må settes.")
        val stringValue = configValue.getString()
        if (stringValue.isBlank()) {
            return if (optional) null else throw IllegalArgumentException("$key må settes.")
        }
        logger.info("{}={}", key, if (secret) "***" else stringValue)
        return stringValue
    }
    internal fun getRequiredString(key: String, secret: Boolean = false) : String = getString(key, secret, false)!!
    internal fun getOptionalString(key: String, secret: Boolean = false) : String? = getString(key, secret, true)
    internal fun <T>getListFromCsv(csv: String, builder: (value: String) -> T) : List<T> = csv.replace(" ", "").split(",").map(builder)
}

@KtorExperimentalAPI
fun ApplicationConfig.getRequiredString(key: String, secret: Boolean) : String = ApplicationConfigExt(this).getRequiredString(key, secret)
@KtorExperimentalAPI
fun ApplicationConfig.getOptionalString(key: String, secret: Boolean) : String? = ApplicationConfigExt(this).getOptionalString(key, secret)
@KtorExperimentalAPI
fun <T>ApplicationConfig.getRequiredList(key : String, secret: Boolean, builder: (value: String) -> T) : List<T> {
    val ext = ApplicationConfigExt(this)
    val csv= ext.getRequiredString(key, secret)
    return ext.getListFromCsv(csv = csv, builder = builder)
}
@KtorExperimentalAPI
fun <T>ApplicationConfig.getOptionalList(key : String, secret: Boolean, builder: (value: String) -> T) : List<T> {
    val ext = ApplicationConfigExt(this)
    val csv = ext.getOptionalString(key, secret) ?: return emptyList()
    return ext.getListFromCsv(csv = csv, builder = builder)
}
@KtorExperimentalAPI
fun ApplicationConfig.id() : String = ApplicationConfigExt(this).getRequiredString("ktor.application.id", secret = false)


