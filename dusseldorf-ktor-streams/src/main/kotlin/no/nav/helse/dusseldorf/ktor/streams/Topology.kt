package no.nav.helse.dusseldorf.ktor.streams

import io.prometheus.client.Counter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.helse.dusseldorf.ktor.core.Retry
import org.apache.kafka.streams.processor.RecordContext
import org.apache.kafka.streams.processor.TopicNameExtractor
import java.time.Duration

private object StreamCounter {
    private val counter = Counter.build()
            .name("stream_processing_status_counter")
            .help("Teller for status av prosessering av meldinger på streams.")
            .labelNames("stream", "status")
            .register()
    internal fun ok(stream: String) = counter.labels(stream, "OK").inc()
    internal fun feil(stream: String) = counter.labels(stream, "FEIL").inc()
}

fun process(
        steg: String,
        id: Pair<String, String>,
        entry: TopicEntry,
        exceptionHandler: (cause: Throwable) -> Unit = { throw it } ,
        block: suspend() -> Data) : TopicEntry {
    return runBlocking(MDCContext(mapOf(
            "correlation_id" to entry.metadata.correlationId,
            "request_id" to entry.metadata.requestId,
            "steg" to steg,
            id.first to id.second
    ))) {
        val processed = try {
            Retry.retry(
                    operation = steg,
                    initialDelay = Duration.ofSeconds(30),
                    maxDelay = Duration.ofSeconds(60)
            ) { block() }
        } catch (cause: Throwable) {
            StreamCounter.feil(steg)
            exceptionHandler(cause)
            null
        }

        /**
         * Om prosessering er OK
         * setter vi steget som utført
         * Ellers returneres den initielle entryen.
         */
        if (processed != null) {
            StreamCounter.ok(steg)
            TopicEntry(
                    metadata = entry.metadata.copy(utførtSteg = steg),
                    data = processed
            )
        } else {
            entry
        }
    }
}

class StegTopicNameExtractor(private val steg: Steg) : TopicNameExtractor<String, TopicEntry> {
    private val forrigeSteg = requireNotNull(steg.forrige){"Kan bare brukes på steg hvor forrige steg er definert."}
    private val dlqTopic = requireNotNull(forrigeSteg.dlqTopic) {"Kan bare brukes på steg hvor forrige steg har en dlqTopic."}
    override fun extract(key: String, entry: TopicEntry, context: RecordContext): String {
        return when(entry.metadata.utførtSteg) {
            steg.navn -> steg.topic
            forrigeSteg.navn -> dlqTopic
            else -> throw IllegalStateException("Har ingen måte å håndtere TopicEntry med utførtSteg ${entry.metadata.utførtSteg}")
        }
    }
}