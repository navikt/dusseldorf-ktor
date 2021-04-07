package no.nav.helse.dusseldorf.ktor.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.stream.Collectors.joining
import java.util.stream.Stream

object EnvironmentUtils {
    private val LOGGER: Logger = LoggerFactory.getLogger(EnvironmentUtils::class.java)

    const val NAIS_APP_NAME_PROPERTY_NAME = "NAIS_APP_NAME"
    const val NAIS_POD_ID_PROPERTY_NAME = "HOSTNAME"
    const val NAIS_NAMESPACE_PROPERTY_NAME = "NAIS_NAMESPACE"
    const val NAIS_CLUSTER_NAME_PROPERTY_NAME = "NAIS_CLUSTER_NAME"

    val DEV_CLUSTERS = listOf("dev-fss", "dev-sbs", "dev-gcp")
    val PROD_CLUSTERS = listOf("prod-fss", "prod-sbs", "prod-gcp")

    val isProduction: Boolean
        get() = clusterName?.let { o: String -> PROD_CLUSTERS.contains(o) } ?: false

    val isDevelopment: Boolean
        get() = clusterName?.let { o: String -> DEV_CLUSTERS.contains(o) } ?: false

    val applicationName: String?
        get() = getOptionalProperty(NAIS_APP_NAME_PROPERTY_NAME)

    val podId: String?
        get() = getOptionalProperty(NAIS_POD_ID_PROPERTY_NAME)

    val namespace: String?
        get() = getOptionalProperty(NAIS_NAMESPACE_PROPERTY_NAME)

    val clusterName: String?
        get() = getOptionalProperty(NAIS_CLUSTER_NAME_PROPERTY_NAME)

    fun setProperty(name: String, value: String, type: Type) {
        LOGGER.info("{}={}", name, type.format(value))
        System.setProperty(name, value)
    }

    fun getRequiredProperty(propertyName: String, vararg otherPropertyNames: String): String {
        return getOptionalProperty(propertyName, *otherPropertyNames) ?: throw IllegalStateException(
            createErrorMessage(
                propertyName,
                *otherPropertyNames
            )
        )
    }

    fun getOptionalProperty(propertyName: String, vararg otherPropertyNames: String): String? {
        var propertyValue: String? = getProperty(propertyName)
        if (propertyValue.isNullOrBlank() && otherPropertyNames.isNullOrEmpty()) {
            propertyValue = otherPropertyNames
                .map { getProperty(it) }
                .firstOrNull { !it.isNullOrBlank() }
        }
        return propertyValue
    }

    private fun createErrorMessage(propertyName: String, vararg otherPropertyNames: String): String {
        return if (otherPropertyNames.isNullOrEmpty()) {
            "mangler property: $propertyName"
        } else {
            val properties: String = Stream.concat(
                Stream.of(propertyName),
                Stream.of(*otherPropertyNames)
            ).collect(joining(", "))
            "fant ingen av propertyene: $properties"
        }
    }

    private fun getProperty(propertyName: String): String? {
        return System.getProperty(propertyName, System.getenv(propertyName))
    }

    enum class Type {
        SECRET, PUBLIC;

        fun format(value: String): String {
            return if (this == PUBLIC) {
                value
            } else "*******"
        }
    }
}
