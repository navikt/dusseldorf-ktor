package no.nav.helse.dusseldorf.ktor.streams

import io.prometheus.client.Counter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.helse.dusseldorf.ktor.core.Retry
import java.time.Duration

private object StreamCounter {
    private val counter = Counter.build()
            .name("stream_processing_status_counter")
            .help("Teller for status av prosessering av meldinger på streams.")
            .labelNames("steg", "status")
            .register()
    internal fun ok(steg: String) = counter.labels(steg, "OK").inc()
    internal fun feil(steg: String) = counter.labels(steg, "FEIL").inc()
}

fun process(
        steg: String,
        id: Pair<String, String>,
        entry: TopicEntry,
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
            throw cause
        }
        StreamCounter.ok(steg)
        TopicEntry(
                metadata = entry.metadata.copy(utførtSteg = steg),
                data = processed
        )
    }
}