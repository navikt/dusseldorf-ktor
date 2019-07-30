package no.nav.helse.dusseldorf.ktor.testsupport.jws

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

internal class JwsFunctions {
    internal companion object {

        private const val kid = "0lSiaDXqORApZcv9WPwDiX3_En8="

        private val rsaKey = RSAKey.parse("""
        {
            "kid": "$kid",
            "kty": "RSA",
            "n": "pBjxqwjKaM4YG3Kf9dRugVjWGFLT6w4tdHRaQoipaTzl_891DMx6ccuUMMbjTxdxevsDbYOB0fjcKHHXQ9JW5yVaBxl2hk7FIre3uDeLOqNbbpr7mekwGzqz4YGAkTpNjDoljxS-5v3Dxo5Zr85FFXpdoed4Vs37p3U7FAlc91sZ0TJ0BV1q5k-kkG6UmsEsdp1qZxNsQ_5K1nWxREDxaBUOiIfDfPiHmmRXHEEEKY_AQ00-i97SC4vMu4cW9tCKxiBpKh743qF-GkctCePol5PCjFpy56PFC4PnZjRFgnn80kdSbFOwH8l07unzOiUzKlWkv0b5WXw8h3ydZFFexw",
            "e": "AQAB",
            "d": "gRm-x7iaxemevblob5c5eTnS9j_zybHVwRDpEf9CiTEIIkGs7OzSSETJybYvj0H6Xa6t-7LCp9cKHieyHAGXrTKNqZg2z2OZZL71I1FPkEqE3HfCCkyTNFjyvC-OXrNn3zK_6dmAd2qeY9AKb23wm_0xPPdGjcRwgEaSvCjBozgd8dKgrn8bnALb1V1mGPZt5X648723uW6zBqO94ue73gqp7WrE2AMTG4SaiX-CzO4dSzLI6AUZGnBfF6umyxrZBFR6g2m1zATBa5i0YXrIHXM3RREnFNmcOrcNO3borzNtZCiMW7ZrXIqO8AVnDjNmVzbg5v3f9Ol1U6t1TT0XAQ",
            "p": "2f9NC_bpRpCvZwsZpALy2HQTYkX0b2P6N5zgQkm17PKiukc73AwHbk9YJYzPjTJY0IpRl2pivSTMOTuTuRU3sDxC_yMvuyJ3gI8rEtux_SDf2G2_OfGRtDgpUNuqzMveaoMpnCfxkO4JO5RmuvX99OW7_8wpZqQh6OQLttWJU5E",
            "q": "wLQ4zrqas0YaTYb0SDmbDexP98CRDGj7E3n6JIx9HwsAm98BDxu9w59gSsCvzg0YnlQNYzvy10v9NxHKnvMvahfg_qmcD1o9YwcvzcljUw3dAIVIQ9rHbNZG4wfgAJt1QgkZQrz2KmGBaIdqR57IpUpfnJ5v4S7FpBZPNm7BMNc",
            "dp": "yUTpgc5p-njDOUQKXF9Mj4Q8EVO9JssLziTM-ObNTQOIMqxqG_QPOE2ReLnVNuvxDDlos3_JwhAjbgQPk6Z_T_uTb7Sw8PoVk2CbyEGGx8p-YXiSQZFDkTz5CGqH-6WOqJCI7mACrGjZpWSSpLNR0bX6KWX6I4YOuNMz7Y6hx8E",
            "dq": "I9ulUnqQvNlHnbOGE0Z83sthWgXAN-H1Dnu9Gz31LmiatWZ6yPftiNBIV8ChNiNjuFqFnziRiJSAStYJsSgpY4GMAXdILecp0xqMP6vAyryiqi0i9FVqlIsO58IYYaSL3jzZMXz-BYbdULkaAre-OFutjPRCd1F_v3fTR5q2YkM",
            "qi": "JcR435ZvHWYJTj9oJedcuJjj4LRhL7bZdXU0PxwEwmMYMoF5Sc64dRs5chats1tVM35fjmEhAthxeQv19x_SV5NB0hX8sE_jqIfAKx5nsZbv-3-FQaI68GKlpxdsGqaK0CqAJYAkGtEUeeWi6HAV8Xl68GSzBYMYDtOM8PPJWO4"
        }
        """.trimIndent())

        private val signer = RSASSASigner(rsaKey)
        private val publicJwk = JSONObject().appendField("keys", JSONArray().appendElement(rsaKey.toPublicJWK().toJSONObject().appendField("use", "sig"))).toJSONString()

        internal fun getPublicJwk() = publicJwk

        internal fun generateJwt(
                headers: Map<String, Any> = emptyMap(),
                claims: Map<String, Any> = emptyMap()
        ) : String {
            val headerBuilder = JWSHeader.Builder(JWSAlgorithm.RS256)
            if (!headers.containsKey("kid")) headerBuilder.keyID(kid)
            if (!headers.containsKey("typ")) headerBuilder.type(JOSEObjectType.JWT)
            headers.forEach { key, value -> headerBuilder.customParam(key, value) }

            val claimBuilder = JWTClaimsSet.Builder()
            if (!claims.containsKey("iat")) claimBuilder.issueTime(LocalDateTime.now().toDate())
            if (!claims.containsKey("exp")) claimBuilder.expirationTime(LocalDateTime.now().plusHours(1).toDate())
            if (!claims.containsKey("nbf")) claimBuilder.notBeforeTime(LocalDateTime.now().toDate())
            if (!claims.containsKey("jti")) claimBuilder.jwtID(UUID.randomUUID().toString())
            claims.forEach { key, value -> claimBuilder.claim(key, value) }

            val signedJWT = SignedJWT(headerBuilder.build(), claimBuilder.build())

            signedJWT.sign(signer)
            return signedJWT.serialize()
        }
    }
}
internal fun LocalDateTime.toDate() = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())