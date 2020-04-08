package no.nav.helse.dusseldorf.ktor.streams

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer

interface Metadata {
    fun correlationId() : String
    fun requestId() : String
}

data class DefaultMetadata(
        val type: String? = null,
        val version : Int,
        @JsonProperty("correlationId")
        @JsonAlias(value = ["correlation_id"])
        val correlationId : String,
        @JsonProperty("requestId")
        @JsonAlias(value = ["request_id"])
        val requestId : String
)

data class TopicEntry<V>(val metadata: Metadata, val data: V)

internal data class Topic<V>(
        val name: String,
        val serDes : SerDes<V>
) {
    val keySerializer = StringSerializer()
    val keySerde = Serdes.String()
    val valueSerde = Serdes.serdeFrom(serDes, serDes)
}
