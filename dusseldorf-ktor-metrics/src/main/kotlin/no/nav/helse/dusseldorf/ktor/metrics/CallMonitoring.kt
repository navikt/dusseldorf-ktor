package no.nav.helse.dusseldorf.ktor.metrics

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.request.ApplicationRequest
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import no.nav.helse.dusseldorf.ktor.core.Paths
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.metrics.CallMonitoring")

class CallMonitoring (
        private val configure: Configuration
) {

    init {
        if (configure.app.isNullOrBlank()) {
            throw IllegalArgumentException("app må settes.")
        }
    }

    private val histogram = Histogram
            .build(
                    "received_http_requests_histogram",
                    "Histogram for alle HTTP-requester som treffer ${configure.app}")
            .labelNames("app", "verb", "path")
            .register()

    private val counter = Counter
            .build(
                    "received_http_requests_counter",
                    "Teller for alle HTTP-requester som treffer ${configure.app}")
            .labelNames("app", "verb", "path", "status")
            .register()

    class Configuration {
        var app : String? = null
        var excludePaths : Set<String> = Paths.DEFAULT_EXCLUDED_PATHS
        var overridePaths : Map<Regex, String> = mapOf()
    }

    private suspend fun interceptRequest(context: PipelineContext<Unit, ApplicationCall>) {
        val verb = context.context.request.httpMethod
        val path = getPath(context.context.request)

        configure.overridePaths.forEach {
            path.matches(it.key)
        }

        if (!skipInterception(path = path, httpMethod = verb)) {
            histogram.labels(configure.app, verb.value, path).startTimer().use {
                context.proceed()
            }
        } else {
            context.proceed()
        }
    }

    private suspend fun interceptResponse(context: PipelineContext<Any, ApplicationCall>) {
        val verb = context.context.request.httpMethod
        val path = getPath(context.context.request)

        try {
            if (context.context.response.status() == null) {
                context.proceed()
            }
        } finally {
            if (!skipInterception(path = path, httpMethod = verb)) {
                val httpStatusCode = (context.context.response.status() ?: HttpStatusCode.InternalServerError)
                val httpStatusCodeString = httpStatusCode.value.toString()
                val family = "${httpStatusCodeString[0]}xx"
                val success = if (httpStatusCode.isSuccess()) "success" else "failure"

                counter.labels(configure.app, verb.value, path, httpStatusCodeString).inc()
                counter.labels(configure.app, verb.value, path, family).inc()
                counter.labels(configure.app, verb.value, path, success).inc()
            }
        }
    }

    private fun getPath(request: ApplicationRequest) : String {
        val path = request.path()
        configure.overridePaths.forEach {
            if (path.matches(it.key)) {
                return it.value
            }
        }
        return path
    }

    private fun skipInterception(
            path: String,
            httpMethod: HttpMethod
    ) : Boolean {
        val isSkippedHttpMethod = httpMethod == HttpMethod.Options || httpMethod == HttpMethod.Head
        if (isSkippedHttpMethod) {
            logger.trace("Monitorerer ikke httpMethod='${httpMethod.value}'")
            return true
        }
        val isSkippedPath = configure.excludePaths.contains(path)
        if (isSkippedPath) {
            logger.trace("Monitorerer ikke path='$path'")
            return true
        }
        logger.trace("Monitorerer httpMethod='${httpMethod.value}', path='$path'")
        return false
    }

    companion object Feature :
            ApplicationFeature<ApplicationCallPipeline, Configuration, CallMonitoring> {

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): CallMonitoring {
            val result = CallMonitoring(
                    Configuration().apply(configure)
            )

            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                result.interceptRequest(this)
            }


            pipeline.sendPipeline.intercept(ApplicationSendPipeline.After) {
                result.interceptResponse(this)
            }


            return result
        }

        override val key = AttributeKey<CallMonitoring>("CallMonitoring")
    }
}