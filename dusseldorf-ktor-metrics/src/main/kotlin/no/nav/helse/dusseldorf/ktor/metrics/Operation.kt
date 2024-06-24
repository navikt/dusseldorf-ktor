package no.nav.helse.dusseldorf.ktor.metrics

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Histogram


class Operation {
    companion object {
        private val histogram = Histogram.builder()
                .name ("monitored_operation_histogram")
                .labelNames("app", "operation")
                .register()

        private val counter = Counter.builder()
                .name("monitored_operation_counter")
                .labelNames("app", "operation", "result")
                .register()

        suspend fun <T> monitored(
                app: String,
                operation: String,
                resultResolver: (T) -> Boolean = { true },
                block: suspend () -> T
        ) : T {
            val timer = histogram.labelValues(app, operation).startTimer()
            return try {
                val result = block()
                counter.labelValues(app, operation, resultResolver(result).toResult())
                result
            } catch (cause: Throwable) {
                counter.labelValues(app, operation, false.toResult()).inc()
                throw cause
            } finally {
                timer.observeDuration()
            }
        }
    }
}

private fun Boolean.toResult() = if (this) "success" else "failure"