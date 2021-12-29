package no.nav.helse.dusseldorf.testsupport.jws
import net.minidev.json.JSONObject
import java.time.LocalDateTime
import java.util.*

object IDPorten: Issuer {
    private val privateKeyJwk = """
        {
          "use": "sig",
          "kty": "RSA",
          "kid": "9ee8238c-29a7-406d-bd3b-c23627e1ee13",
          "alg": "RS256",
          "n": "n_t8WdCfLXOg47O-cOFIZU2ucOYq9lgG6VfS5a35totmyeTDBSWtl0StbHrOjjfS5dz_6o430QrvbXrZXCpTFmzV98IzJGZNOyo3vDd0sC2NlAEJ0u6UW8Z7PIlWPwlG4TUB-MyoJygd-MMogbge2lwT6iYN7KrT_eMv2pYN_oWCFB6b0TNRhVK72sAtexG7r8Xxyj9EiLCfKyV2whtBhsgwNSDK-vBAK5Ffazoq-GxWKL1Jd7xBskHAYk4PpDKDKt8Ujy2SmM4Z7lEUi5MizW0mqIoxdxzWy9S_lt8kcyH1nADB9sdq20Ge2MxQnnVoFAKRG3SSUdBlDSKq-UBO9w",
          "e": "AQAB",
          "d": "SP5nuFsVpZlSWVJTnBFOBNqystRx2cBENwdZNG5ytzVXitqTmK-eSpyNPCe0QvN5iGOTaKPHF9wTbjJVJlYxXtP9tHOo_fynVE1o7LmoxhGivVrT7qfojQ88f1xyO4SyEGMtKSXHu0X0luIEg1tMyHimpXkDdW5kFz0nQmagr_XluievooTn1pCKAwAQKrdJCeM8aByZATPMukhW7EzKyP1Mslg1NZZRJWl2jb3tKqvuL7vE9rZzl3I9pVGR3dkGMKGR-U01mexBafgqMr1m07d7VPt2ZBy2bS-8KaQIUibZC_8PF3LtuMUVzgIfXRW18IVZadzR9yJMnGrmDMBwkQ",
          "p": "zLdneECfOqn5BHdPX4Xkgn3BCXHYB_XUCUFi6cQ8zB_mGsqW-M-VrLiOVttdb-Pilo5XA55J8z8JsGx58mFlQRmfxQrTIA_u1vMo3p2G6J1HJWHFwY1mOOitF7kZkIWif36HaPojGhzR2_HxN6Rv3QQvu1w5cobPT4JU7WCjCMM",
          "q": "yA9B_OZxlY0uu9O_RHThtwCRFnqsktbMGN22sQvgsT9LgpwrRhdTDYklannN4LGGm5Ab9G1kCmxAZEaV4XCHVDzGZxq_g66LBi1C66TwDBWfdrdkm3n6myGmqN1gVQ1NOwSPShpd1CmVOH2Nb1DKMAB4fyXLF61hPzICk7M8Xb0",
          "dp": "AYPV8zDfPMrnOd48KyPNhh2yIQTDqYNrN0pRlhNKs-QEiuw24859eBLBkuj6rwpLsfuJr1Arke5Efxa-bEnGOw4afZXP7CvSA6s3ATa_Odd4IFGx6fXG7OLj7QoEo2opwrHIBMHdIqNV9j-7HkNMRKF0TPmovy06OYdoY31Ul4s",
          "dq": "udi6s0YcBBLIdEHrr5tox0kmfxxszmft57enbGcdT2r-KZIL_SvTwoJCGNyYLYCdDExeO72XLruLQdv0hh20CcP_h25otNpjWdSYEzIAsSkxHuaU9n3lGVG0cbBdOgWGVioRNoSx31LVxUmVJvCFMvxs86jXNuKgsUUy8aSSkn0",
          "qi": "R5pz-C-w5Jd-Zb0RZeMuu2p8fmhCaWwLX7LYSU-ocWq74x4d2qgwNOmivYTD-eMUq1ZkdRq7kKc3F3BWVJIkjsnOQ8HWvxtZUdgcF4qrycfQPmW4CWG1_g8W2eEWrR6SVIHzcdks53NplwJZg8f99Dt13T6ow_vAnqorNUe_bQQ"
        }
        """.trimIndent()
    private val jwsFunctions = JwsFunctions(privateKeyJwk)

    private const val audience = "http://localhost/id-porten/v2.0/audience"
    private const val actualIssuer = "http://localhost/id-porten/v2.0/issuer"

    override fun getIssuer() = actualIssuer
    fun getAudience() = audience
    override fun getPublicJwk() = jwsFunctions.getPublicJwk()

    /**
     * Genererer en gyldig idToken.
     * For mer info om claims i idToken, se: [docs.digdir.no/oidc_protocol_id_token](https://docs.digdir.no/oidc_protocol_id_token.html)
     */
    fun generateIdToken(
        fnr: String,
        level: Int = 4,
        issuer: String = actualIssuer,
        expiration: Date = LocalDateTime.now().plusHours(1).toDate(),
        overridingClaims: Map<String, Any> = emptyMap()
    ) = jwsFunctions.generateJwt(
        claims = overridingClaims.toMutableMap().apply {
            putIfAbsent("acr","Level$level")
            putIfAbsent("pid", fnr)
            putIfAbsent("sub", UUID.randomUUID().toString())
            putIfAbsent("aud", getAudience())
            putIfAbsent("iss", issuer)
            putIfAbsent("amr", listOf("BankID"))
            putIfAbsent("sid", UUID.randomUUID().toString())
            putIfAbsent("jti", UUID.randomUUID().toString())
            putIfAbsent("locale", "nb")
            putIfAbsent("auth_time", LocalDateTime.now().toDate())
            putIfAbsent("iat", LocalDateTime.now().toDate())
            putIfAbsent("exp", expiration)
        }.toMap()
    )

    /**
     * Genererer en gyldig accessToken.
     * For mer info om claims i accessToken, se: [docs.digdir.no/oidc_protocol_access_token](https://docs.digdir.no/oidc_protocol_access_token.html)
     */
    fun generateAccessToken(
        fnr: String,
        level: Int = 4,
        issuer: String = actualIssuer,
        expiration: Date = LocalDateTime.now().plusHours(1).toDate(),
        overridingClaims: Map<String, Any> = emptyMap()
    ) = jwsFunctions.generateJwt(
        claims = overridingClaims.toMutableMap().apply {
            putIfAbsent("pid", fnr)
            putIfAbsent("acr","Level$level")
            putIfAbsent("sub", UUID.randomUUID().toString())
            putIfAbsent("iss", issuer)
            putIfAbsent("client_amr", "private_key_jwt")
            putIfAbsent("token_type", "Bearer")
            putIfAbsent("client_id", UUID.randomUUID().toString())
            putIfAbsent("scope", "openid")
            putIfAbsent("client_orgno", "889640782")
            putIfAbsent("jti", UUID.randomUUID().toString())
            putIfAbsent("iat", LocalDateTime.now().toDate())
            putIfAbsent("exp", expiration)
            putIfAbsent("consumer", JSONObject(mapOf(
                "authority" to "iso6523-actorid-upis",
                "ID" to "0192:889640782"
            )).toString())
        }.toMap()
    )
}
