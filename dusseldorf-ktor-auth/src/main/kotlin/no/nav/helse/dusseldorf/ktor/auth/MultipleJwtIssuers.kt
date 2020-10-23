package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import io.ktor.application.ApplicationCall
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.auth.MultipleJwtIssuers")
private const val AUTH_SCHEME = "Bearer "
private const val FALLBACK_ALIAS = "fallback"

fun Authentication.Configuration.multipleJwtIssuers(
    issuers : Map<Issuer, Set<ClaimRule>>,
    extractHttpAuthHeader: (ApplicationCall) -> HttpAuthHeader? = { call ->
        call.request.parseAuthorizationHeaderOrNull()
    }) {

    issuers.forEach { (issuer, additionalClaimRules) ->
        val jwkProvider = JwkProviderBuilder(issuer.jwksUri().toURL())
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

        val claimRules = issuer.asClaimRules()
        claimRules.addAll(additionalClaimRules)
        val claimEnforcer = ClaimEnforcer(setOf(claimRules))

        jwt (issuer.alias()) {
            realm = issuer.alias()
            verifier (jwkProvider, issuer.issuer()) {
                acceptNotBefore(10)
                acceptIssuedAt(10)
            }
            authHeader { call ->
                logger.info("Issuer[${issuer.alias()}]")
                val httpAuthHeader = extractHttpAuthHeader(call)
                val jwt = httpAuthHeader?.decodeJwtOrNull()
                if (httpAuthHeader != null && jwt != null) {
                    logger.trace("Utfører authorization sjekk av Authorization Header.")
                    val success = claimEnforcer.enforce(jwt.claims)
                    logger.trace("${success.size} håndhevelser av claims ble sjekket og er OK.")
                }
                httpAuthHeader
            }
            skipWhen { call -> tokenIsAbsentOrNotIssuedBy(extractHttpAuthHeader(call), issuer.issuer()) }
            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }

    val configuredIssuers = issuers.keys.map { it.issuer() }

    jwt(FALLBACK_ALIAS) {
        skipWhen { call ->
            val httpAuthHeader = extractHttpAuthHeader(call)
            val skipping = tokenIsSetAndIssuerIsOneOf(httpAuthHeader, configuredIssuers)
            if (!skipping) {
                val token = httpAuthHeader?.decodeJwtOrNull()
                if (token != null) logger.error("Request med token utstedt av '${token.issuer}' stoppes da issuer ikke er konfigurert. Token uten signatur = '${token.header}.${token.payload}'")
                else {
                    val authorizationHeader = call.request.header(HttpHeaders.Authorization)
                    if (authorizationHeader != null) logger.error("Request med ugylidig format på Authorization header stoppes. Authorization header = '$authorizationHeader'")
                    else logger.error("Request uten Authorization header satt stoppes.")
                }
            }
            skipping
        }
        validate { return@validate null }
    }
}

private fun tokenIsAbsentOrNotIssuedBy(httpAuthHeader: HttpAuthHeader?, issuer: String) =
    httpAuthHeader?.decodeJwtOrNull()?.issuer == issuer

private fun tokenIsSetAndIssuerIsOneOf(httpAuthHeader: HttpAuthHeader?, otherIssuers: List<String>) =
    httpAuthHeader?.decodeJwtOrNull()?.issuer?.let { otherIssuers.contains(it) } ?: false

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
    issuers.add(FALLBACK_ALIAS)
    return issuers.toTypedArray()
}