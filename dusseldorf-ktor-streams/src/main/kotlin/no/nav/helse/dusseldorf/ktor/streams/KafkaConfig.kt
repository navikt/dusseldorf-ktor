package no.nav.helse.dusseldorf.ktor.streams

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.dusseldorf.ktor.core.id
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(KafkaConfig::class.java)

class KafkaConfig(
        private val appId: String,
        bootstrapServers: String,
        credentials: Pair<String, String>,
        trustStore: Pair<String, String>?,
        val unreadyAfterStreamStoppedIn: Duration
) {
    private val streams = Properties().apply {
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler::class.java)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        medCredentials(credentials)
        medTrustStore(trustStore)
        medProcessingGuarantee()
    }

    private val producers = Properties().apply {
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        medCredentials(credentials)
        medTrustStore(trustStore)
    }

    fun stream(name: String) = streams.apply {
        put(APPLICATION_ID_CONFIG, "$appId-$name")
    }

    fun producer(name: String) = producers.apply {
        put(ProducerConfig.CLIENT_ID_CONFIG, "$appId-$name")
    }
}

private fun Properties.medProcessingGuarantee() {
    logger.info("$PROCESSING_GUARANTEE_CONFIG=$EXACTLY_ONCE")
    put(PROCESSING_GUARANTEE_CONFIG, EXACTLY_ONCE)
    put(REPLICATION_FACTOR_CONFIG, "3")
}
private fun Properties.medTrustStore(trustStore: Pair<String, String>?) {
    trustStore?.let {
        try {
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(it.first).absolutePath)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, it.second)
            logger.info("Truststore på '${it.first}' konfigurert.")
        } catch (cause: Throwable) {
            logger.error(
                    "Feilet for konfigurering av truststore på '${it.first}'",
                    cause
            )
        }
    }
}
private fun Properties.medCredentials(credentials: Pair<String, String>) {
    put(SaslConfigs.SASL_MECHANISM, "PLAIN")
    put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
    put(
            SaslConfigs.SASL_JAAS_CONFIG,
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${credentials.first}\" password=\"${credentials.second}\";"
    )
}


@KtorExperimentalAPI
fun ApplicationConfig.kafkaConfig() : KafkaConfig {
    val bootstrapServers = getRequiredString("nav.kafka.bootstrap_servers", secret = false)

    val trustStore = getOptionalString("nav.trust_store.path", secret = false)?.let { trustStorePath ->
        getOptionalString("nav.trust_store.password", secret = true)?.let { trustStorePassword ->
            Pair(trustStorePath, trustStorePassword)
        }
    }

    val unreadyAfterStreamStoppedIn = Duration.of(
            getRequiredString("nav.kafka.unready_after_stream_stopped_in.amount", secret = false).toLong(),
            ChronoUnit.valueOf(getRequiredString("nav.kafka.unready_after_stream_stopped_in.unit", secret = false))
    )

    return KafkaConfig(
            appId = id(),
            bootstrapServers = bootstrapServers,
            credentials = Pair(
                    getRequiredString("nav.kafka.username", secret = false),
                    getRequiredString("nav.kafka.password", secret = true)),
            trustStore = trustStore,
            unreadyAfterStreamStoppedIn = unreadyAfterStreamStoppedIn
    )
}