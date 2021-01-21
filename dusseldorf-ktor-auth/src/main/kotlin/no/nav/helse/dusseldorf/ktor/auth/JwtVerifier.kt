package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.slf4j.LoggerFactory
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

class JwtVerifier(
    private val issuer: Issuer,
    additionalClaimRules : Set<ClaimRule> = emptySet()) {

    private val verifiers : MutableMap<String, JWTVerifier> = mutableMapOf()

    private val claimEnforcer = ClaimEnforcer(setOf(issuer.asClaimRules().plus(additionalClaimRules)))

    private val jwkProvider = JwkProviderBuilder(issuer.jwksUri().toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    fun verify(jwt: String) : Boolean {
        val decoded = kotlin.runCatching { JWT.decode(jwt) }.fold(
            onSuccess = {it},
            onFailure = {
                logger.warn("Kunne ikke decodes som en JWT", it)
                return false
            }
        )
        kotlin.runCatching { signature(decoded) }.fold(
            onSuccess = {},
            onFailure = {
                logger.warn("Signatur på JWT kunne ikke verifiseres", it)
                return false
            }
        )

        return kotlin.runCatching { claims(decoded) }.fold(
            onSuccess = {true},
            onFailure = {
                logger.warn("Feilet ved sjekk på claims i JWT", it)
                false
            }
        )
    }

    private fun signature(decoded: DecodedJWT) {
        val jwk = jwkProvider.get(decoded.keyId)
        val verifier = verifiers[jwk.id] ?: jwk.nyVerifier()
        verifier.verify(decoded)
    }

    private fun claims(decoded: DecodedJWT) = claimEnforcer.enforce(decoded.claims)

    private fun Jwk.nyVerifier() : JWTVerifier {
        val algorithm = Algorithm.RSA256(publicKey as RSAPublicKey, null)
        val verifier = JWT.require(algorithm).withIssuer(issuer.issuer())
            .acceptNotBefore(10)
            .acceptIssuedAt(10)
            .build()
        verifiers[id] = verifier
        return verifier
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(JwtVerifier::class.java)
    }
}