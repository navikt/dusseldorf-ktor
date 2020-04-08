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
            .labelNames("stream", "status")
            .register()
    internal fun ok(name: String) = counter.labels(name, "OK").inc()
    internal fun feil(name: String) = counter.labels(name, "FEIL").inc()
}

fun <BEFORE, AFTER>process(
        name: String,
        id: Pair<String, String>,
        entry: TopicEntry<BEFORE>,
        block: suspend() -> AFTER) : TopicEntry<AFTER> {
    return runBlocking(MDCContext(mapOf(
            "correlation_id" to entry.metadata.correlationId(),
            "request_id" to entry.metadata.requestId(),
            id.first to id.second
    ))) {
        val processed = try {
            Retry.retry(
                    operation = name,
                    initialDelay = Duration.ofSeconds(30),
                    maxDelay = Duration.ofSeconds(60)
            ) { block() }
        } catch (cause: Throwable) {
            StreamCounter.feil(name)
            throw cause
        }
        StreamCounter.ok(name)
        TopicEntry(
                metadata = entry.metadata,
                data = processed
        )
    }
}