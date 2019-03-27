package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.feature
import io.ktor.routing.*

fun Route.requiresCallId(
        build: Route.() -> Unit
): Route {
    val requiresCallIdRoutes = createChild(RequiresCallIdRouteSelector())
    application.feature(CallIdRequired).interceptPipeline(requiresCallIdRoutes)
    requiresCallIdRoutes.build()
    return requiresCallIdRoutes
}

private class RequiresCallIdRouteSelector : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }
}