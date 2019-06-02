package no.nav.helse.dusseldorf.ktor.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

class Operation {
    companion object {
        private val histogram = Histogram
                .build("monitored_operation_histogram",
                        "Histogram som måler operasjoner.")
                .labelNames("operation")
                .register()

        private val counter = Counter
                .build(
                        "monitored_operation_counter",
                        "Teller for alle målte operasjoenr.")
                .labelNames("operation", "result")
                .register()

        suspend fun <T> monitored(
                name: String,
                resultResolver: (T) -> Boolean = { true },
                block: suspend () -> T
        ) : T {
            val timer = histogram.labels(name).startTimer()
            return try {
                val result = block()
                counter.labels(name, resultResolver(result).toResult())
                result
            } catch (cause: Throwable) {
                counter.labels(name, false.toResult()).inc()
                throw cause
            } finally {
                timer.observeDuration()
            }
        }
    }
}

private fun Boolean.toResult() = if (this) "success" else "failure"