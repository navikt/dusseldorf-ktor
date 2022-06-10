package no.nav.helse.dusseldorf.ktor.auth

import io.ktor.server.plugins.statuspages.StatusPagesConfig
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.respondProblemDetails
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.auth.AuthStatusPages")
private val problemDetails = DefaultProblemDetails(
        title = "unauthorized",
        status = 403,
        detail = "Requesten inneholder ikke tilstrekkelige tilganger."
)

fun StatusPagesConfig.AuthStatusPages() {
    exception<ClaimEnforcementFailed> { call, cause ->
        logger.error("Request uten tilstrekkelig tilganger stoppet. Håndheving av regler resulterte i '${cause.outcomes}'")
        call.respondProblemDetails(problemDetails, logger)
    }
}
