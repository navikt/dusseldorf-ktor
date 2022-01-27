package no.nav.helse.dusseldorf.ktor.auth

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import no.nav.helse.dusseldorf.ktor.auth.ClaimEnforcementFailed
import no.nav.helse.dusseldorf.ktor.auth.CookieNotSetException
import no.nav.helse.dusseldorf.ktor.auth.IdTokenInvalidFormatException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/*
    In summary, a 401 Unauthorized response should be used for missing or bad authentication,
    and a 403 Forbidden response should be used afterwards, when the user is authenticated but
    isn’t authorized to perform the requested operation on the given resource.
 */

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.auth.AuthorizationStatusPagesKt.IdTokenStatusPages")

fun StatusPages.Configuration.IdTokenStatusPages() {

    exception<ClaimEnforcementFailed> { cause ->
        call.respond(HttpStatusCode.Forbidden)
        // Kan ha vært logget inn på en annen NAV-tjeneste som ikke krever nivå 4 i forkant
        // og må nå logge inn på nytt. Trenger ikke å logge noe error på dette
        logger.trace(cause.message)
    }

    exception<CookieNotSetException> { cause ->
        call.respond(HttpStatusCode.Unauthorized)
        logger.trace(cause.message)
    }

    exception<UnAuthorizedException> { cause ->
        call.respond(HttpStatusCode.Unauthorized)
        logger.trace(cause.message)
    }

    exception<IdTokenInvalidFormatException> { cause ->
        call.respond(HttpStatusCode.Unauthorized)
        logger.error(cause.message)
    }
}
