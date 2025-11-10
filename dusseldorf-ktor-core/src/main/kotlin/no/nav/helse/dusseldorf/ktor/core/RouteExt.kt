package no.nav.helse.dusseldorf.ktor.core

import io.ktor.http.HttpHeaders
import io.ktor.server.application.PipelineCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.plugins.callid.callId
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.RouteExt")

fun Route.requiresCallId(build: Route.() -> Unit): Route {
    return createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }
        override fun toString(): String = "RequiresCallId"
    }).apply {
        install(CallIdRequiredPlugin)
        build()
    }
}

// Definerer route-scoped plugin som sjekker at det eksisterer callId på request.
val CallIdRequiredPlugin = createRouteScopedPlugin("CallIdRequiredPlugin") {
    val problemDetails = ValidationProblemDetails(
        setOf(Violation(
            parameterName = HttpHeaders.XCorrelationId,
            parameterType = ParameterType.HEADER,
            reason = "Correlation ID må settes.",
            invalidValue = null
        ))
    )

    onCall { call: PipelineCall ->
        if (call.callId == null) {
            call.respondProblemDetails(problemDetails, logger, null)
            return@onCall
        }
    }
}
