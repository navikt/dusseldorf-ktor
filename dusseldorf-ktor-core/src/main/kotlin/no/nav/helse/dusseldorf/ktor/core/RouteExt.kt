package no.nav.helse.dusseldorf.ktor.core

import io.ktor.server.application.plugin
import io.ktor.server.routing.*

fun Route.requiresCallId(
        build: Route.() -> Unit
): Route {
    val requiresCallIdRoutes = createChild(RequiresCallIdRouteSelector())
    application.plugin(CallIdRequired).interceptPipeline(requiresCallIdRoutes)
    requiresCallIdRoutes.build()
    return requiresCallIdRoutes
}

private class RequiresCallIdRouteSelector : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString() = "RequiresCallId"
}
