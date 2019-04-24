package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import io.ktor.application.ApplicationCall
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.http.HttpHeaders
import io.ktor.request.header
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.auth.MultipleJwtIssuers")


fun Authentication.Configuration.multipleJwtIssuers(
        issuers : Map<Issuer, Set<ClaimRule>>
) {
    issuers.forEach { issuer, additionalClaimRules ->
        val otherIssuers = otherIssuers(issuer, issuers.keys)

        val jwkProvider = JwkProviderBuilder(issuer.jwksUri())
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

        val claimRules = issuer.asClaimRules()
        claimRules.addAll(additionalClaimRules)
        val claimEnforcer = ClaimEnforcer(setOf(claimRules))

        jwt (issuer.issuer()) {
            verifier (jwkProvider, issuer.issuer())
            skipWhen { otherIssuers.isNotEmpty() && it.tokenIsSetAndIssuerIsOneOf(otherIssuers) }
            validate { credentials ->
                logger.trace("Utfører authorization sjekk av Authorization Header.")
                val success = claimEnforcer.enforce(credentials.payload.claims)
                logger.trace("${success.size} håndhevelser av claims ble sjekket og er OK.")
                JWTPrincipal(credentials.payload)
            }
        }
    }
}

fun otherIssuers(
        currentIssuer : Issuer,
        allIssuers : Set<Issuer>
) : List<String> {
    val otherIssuers = mutableListOf<String>()
    allIssuers.filter { it.issuer() != currentIssuer.issuer() }.forEach { otherIssuers.add(it.issuer()) }
    return otherIssuers.toList()
}

fun ApplicationCall.tokenIsSetAndIssuerIsOneOf(otherIssuers: List<String>): Boolean {
    val token = request.header(HttpHeaders.Authorization)?.removePrefix("Bearer ") ?: return false
    return try {otherIssuers.contains(JWT.decode(token).issuer)} catch (cause: Throwable) { false }
}