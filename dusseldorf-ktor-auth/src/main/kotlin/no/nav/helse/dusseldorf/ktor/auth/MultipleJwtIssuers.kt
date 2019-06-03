package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import io.ktor.application.ApplicationCall
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.request.ApplicationRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.auth.MultipleJwtIssuers")
private const val AUTH_SCHEME = "Bearer "

fun Authentication.Configuration.multipleJwtIssuers(
        issuers : Map<Issuer, Set<ClaimRule>>
) {
    issuers.forEach { issuer, additionalClaimRules ->
        val otherIssuers = otherIssuers(issuer, issuers.keys)

        val jwkProvider = JwkProviderBuilder(issuer.jwksUri().toURL())
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

        val claimRules = issuer.asClaimRules()
        claimRules.addAll(additionalClaimRules)
        val claimEnforcer = ClaimEnforcer(setOf(claimRules))

        jwt (issuer.alias()) {
            verifier (jwkProvider, issuer.issuer())
            authHeader { call ->
                val httpAuthHeader = call.request.parseAuthorizationHeaderOrNull()
                val jwt = httpAuthHeader?.decodeJwtOrNull()
                if (httpAuthHeader != null && jwt != null) {
                    logger.trace("Utfører authorization sjekk av Authorization Header.")
                    val success = claimEnforcer.enforce(jwt.claims)
                    logger.trace("${success.size} håndhevelser av claims ble sjekket og er OK.")
                }
                httpAuthHeader
            }
            skipWhen { call -> otherIssuers.isNotEmpty() && call.tokenIsSetAndIssuerIsOneOf(otherIssuers) }
            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }
}

private fun otherIssuers(
        currentIssuer : Issuer,
        allIssuers : Set<Issuer>
) = allIssuers.filter { it.issuer() != currentIssuer.issuer() }.map { it.issuer() }

private fun ApplicationCall.tokenIsSetAndIssuerIsOneOf(otherIssuers: List<String>): Boolean {
    val jwt = request.parseAuthorizationHeaderOrNull()?.decodeJwtOrNull() ?: return false
    return otherIssuers.contains(jwt.issuer)
}

private fun ApplicationRequest.parseAuthorizationHeaderOrNull() = try {
    parseAuthorizationHeader()
} catch (cause: Throwable) {
    logger.error("Ugyldig Authorization Header", cause)
    null
}
private fun HttpAuthHeader.decodeJwtOrNull() = try {
    JWT.decode(render().removePrefix(AUTH_SCHEME))
} catch (cause: Throwable) {
    logger.error("Authorization Header ikke en JWT", cause)
    null
}

fun Map<String, Issuer>.withoutAdditionalClaimRules() = withAdditionalClaimRules(mapOf())

fun Map<String, Issuer>.withAdditionalClaimRules(
        additionalClaimRules: Map<String, Set<ClaimRule>>
) : Map<Issuer, Set<ClaimRule>> {
    val result = mutableMapOf<Issuer, Set<ClaimRule>>()
    forEach { alias , issuer -> result[issuer] = additionalClaimRules.getOrDefault(alias, setOf()) }
    return result.toMap()
}

fun Map<Issuer, Set<ClaimRule>>.allIssuers() : Array<String> {
    val issuers = mutableListOf<String>()
    forEach { issuer, _ -> issuers.add(issuer.alias())}
    return issuers.toTypedArray()
}