package no.nav.helse.dusseldorf.ktor.streams

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

private object SerDesUtils {
    internal val objectMapper = jacksonObjectMapper()
            .dusseldorfConfigured()
}

abstract class SerDes<V>(private val objectMapper: ObjectMapper = SerDesUtils.objectMapper)
    : Serializer<V>, Deserializer<V> {
    override fun serialize(topic: String?, data: V): ByteArray? {
        return data?.let {
            objectMapper.writeValueAsBytes(it)
        }
    }
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}