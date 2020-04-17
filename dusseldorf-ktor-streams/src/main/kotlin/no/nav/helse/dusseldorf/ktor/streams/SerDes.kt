package no.nav.helse.dusseldorf.ktor.streams

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.json.JSONObject
import java.time.ZonedDateTime

class TopicEntrySerDes : Serializer<TopicEntry>, Deserializer<TopicEntry> {
    companion object {
        private val keySerde = Serdes.String()
        private val valueSerde = Serdes.serdeFrom(TopicEntrySerDes(), TopicEntrySerDes())
        val produced = Produced.with(keySerde, valueSerde)
        val consumed = Consumed.with(keySerde, valueSerde)
    }
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
    override fun serialize(topic: String, entry: TopicEntry): ByteArray = entry.rawJson.toByteArray()
    override fun deserialize(topic: String, entry: ByteArray): TopicEntry = TopicEntry(String(entry))
}

data class TopicEntry(val rawJson: String) {
    constructor(metadata: Metadata, data: Data) : this(
            JSONObject(mapOf(
                    "metadata" to JSONObject(mapOf(
                            "opprettet" to metadata.opprettet.toString(),
                            "utførtSteg" to metadata.utførtSteg,
                            "versjon" to metadata.versjon,
                            "correlationId" to metadata.correlationId,
                            "requestId" to metadata.requestId
                    )),
                    "data" to JSONObject(data.rawJson)
            )).toString()
    )
    private val entityJson = JSONObject(rawJson)
    private val metadataJson = requireNotNull(entityJson.getJSONObject("metadata"))
    private val dataJson = requireNotNull(entityJson.getJSONObject("data"))
    val metadata = Metadata(
            opprettet = ZonedDateTime.parse(requireNotNull(metadataJson.getString("opprettet"))),
            utførtSteg = requireNotNull(metadataJson.getString("utførtSteg")),
            versjon = requireNotNull(metadataJson.getInt("versjon")),
            correlationId = requireNotNull(metadataJson.getString("correlationId")),
            requestId = requireNotNull(metadataJson.getString("requestId"))
    )
    val data = Data(dataJson.toString())
}
data class Data(val rawJson: String) {

}
data class Metadata(
        val opprettet: ZonedDateTime,
        val utførtSteg: String,
        val versjon : Int,
        val correlationId : String,
        val requestId : String
)