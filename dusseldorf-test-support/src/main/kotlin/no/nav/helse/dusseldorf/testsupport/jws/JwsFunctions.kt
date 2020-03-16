package no.nav.helse.dusseldorf.testsupport.jws

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

internal class JwsFunctions(
        privateKeyJwk: String
) {
    private val rsaKey = RSAKey.parse(privateKeyJwk)
    private val signer = RSASSASigner(rsaKey)


    private val publicJwk = JSONObject().apply {
        val key = rsaKey.toPublicJWK().toJSONObject().apply {
            this["use"] = "sig"
        }
        val keys = JSONArray().apply {
            this.add(key)
        }
        this["keys"] = keys
    }.toJSONString()


    internal fun getPublicJwk() = publicJwk

    internal fun generateJwt(
            headers: Map<String, Any> = emptyMap(),
            claims: Map<String, Any> = emptyMap()
    ) : String {
        val headerBuilder = JWSHeader.Builder(JWSAlgorithm.RS256)
        if (!headers.containsKey("kid")) headerBuilder.keyID(rsaKey.keyID)
        if (!headers.containsKey("typ")) headerBuilder.type(JOSEObjectType.JWT)
        headers.forEach { (key, value) -> headerBuilder.customParam(key, value) }

        val claimBuilder = JWTClaimsSet.Builder()
        if (!claims.containsKey("iat")) claimBuilder.issueTime(LocalDateTime.now().toDate())
        if (!claims.containsKey("exp")) claimBuilder.expirationTime(LocalDateTime.now().plusHours(1).toDate())
        if (!claims.containsKey("nbf")) claimBuilder.notBeforeTime(LocalDateTime.now().toDate())
        if (!claims.containsKey("jti")) claimBuilder.jwtID(UUID.randomUUID().toString())
        claims.forEach { (key, value) -> claimBuilder.claim(key, value) }

        val signedJWT = SignedJWT(headerBuilder.build(), claimBuilder.build())

        signedJWT.sign(signer)
        return signedJWT.serialize()
    }
}

internal fun LocalDateTime.toDate() = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())