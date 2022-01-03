package no.nav.helse.dusseldorf.testsupport.jws

import com.nimbusds.jwt.SignedJWT

object Tokendings : Issuer {
    private val privateKeyJwk = """
    {
        "kty": "RSA",
        "n": "rHuAnXVRyMe5E7sWHS8zrJLiRjtvzFeB-pK1gTMcZOg2_VHaARIl5E5_47uzQGa99jXEDSX0fStpxkgr7EZ1eCV7z0d6lsBUzFdERSR6yLfOiq4TuuvYrZ-6Lkj65vlu3qK4tPCZCMk748XpPp7elWq1nUX3Nt6gP0I9EG67Qah7IJa_FNWWIPTIfsbqD64BuUBI_YZLZU9ZPuQup-ctpnGuENcvqbs2plMn-EdzddxZ8U5qyCJCY-nAypLr--88tbVqnt3kuFRRsN_HfWitlM_bXqbDF89HfFT1QFKbvqnQ6j5Z3BZOnpdLbg5VM8dyi-blUolcoUB9GyrdXsyNbw",
        "e": "AQAB",
        "d": "IRMBo6_2gdDCjaUHZYtClmoIF72X_5AcLYdqZJ7_Z94bhVHnwuLO_0uKPIomokfRT2AZ-wd3LXVgsBPw0DrmlaZiamnjePrih9iID9Yh8VFFutQMloBZKVoCopN-AUJ9XxXG43NrdQLZiCDQTCC8v9wZfVIISK5B_j9eZENHhxxNv0X1axl0Uts4cld1rzWIFYVchpa6jhJyK7U4jwhD-SNwp-RAjiXiUacywyMKror0BXfuIId0KA24cI65GqFBiwKFpPt-f_LvYxwZuprDk7CJ5kAfaJHBqskFpPYv1YfLhXyVaLnV0oXNOCI_zed_4KlRGmiPtMBidjGhesw-sQ",
        "p": "46Do9jQlF4XNRtPg6CpxYCUwEoc6SMNjQjs4WcP_VFo_1FHUj2TtQLaseYlVUFmKTxds0cSGOJes3PrXcgUtT2ZINSXKSwNbBdYlONplMt54KbYbCIGWgSZqYlZt1pTVG8EyW6P7ebNtMdOwJ0NgWXACppRR1kli586SBtCb4vk",
        "q": "wfsCfigH1gLKOO6sj6ml1ycMz1NgS_N8qBeV_RjaXPyY-MjUs32cGpKBVU3jVM2yUt-q49OlEaaN6jj8KSAVI2Osgg6TO-5Zev3WjMn-R_WXcU-wGQME2EA6S-a9ViC7kassZ7aaQqYddHp_lr5oFJAfnEViUk9O1y_fTrGZpac",
        "dp": "JiBQhNRtFgiKPn_Env5k6Qaxi5js0T0wxeonYRmsDZPkNwrRwIbWGk2-i8jN6lxlByRUTLpPBPGauZSmO2nUkgT-uTAwNupD97FLoL8rzh5nIA1pb75M2Cr4DKqPUS6ylyh_58uKlnLQKlVbnufxHmndd8tF_c9i6pK18MBGK_k",
        "dq": "GsTc7zPvAOWZqomlXp32uN42slW7MEAFr9yxYiAP4k0pfwuX7r0i0bfuCSuad5V7Slx7ZL1MRkdJxkze334B7MRQhhIk11xpFMP626r0YFHP-F8a2hjEjmMuqFLLRht2Rv_sPNPCLfhmkDBncxILourQsewRSXKQWrnMgzuXDHk",
        "qi": "WrYI5_eAmYkRTOmJ64-vTCztIKk5Ln3cS5DW0SAnIwz1Qe-0DMEoQdObY7o2oQvu_jpTcltM4SFSLsFRW4AJWMY0Md3LMShVwwba83laHOODEzsmNqOvQi2lrghDNsmRVh9Zmnyva8c_w1qLCrfzHd8q82G7OyajvGCnB3jxIj8"
    }
    """.trimIndent()

    private val jwsFunctions = JwsFunctions(privateKeyJwk)

    private const val actualIssuer = "http://localhost/tokendings/issuer"
    private const val audience = "http://localhost/tokendings/audience"

    override fun getPublicJwk() : String = jwsFunctions.getPublicJwk()
    override fun getIssuer() = actualIssuer
    fun getAudience() = audience

    private fun String.urlDecodedBodyAsMap() = split("&").associate {
        it.split("=")[0] to it.split("=")[1]
    }.also {
        check(it.contains("client_assertion"))
        check(it.contains("subject_token"))
        check(it.contains("audience"))
        // https://doc.nais.io/security/auth/tokenx/#exchange-request
        check(it.getValue("grant_type") == "urn:ietf:params:oauth:grant-type:token-exchange")
        check(it.getValue("client_assertion_type") == "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
        check(it.getValue("subject_token_type") == "urn:ietf:params:oauth:token-type:jwt")
    }

    fun generateJwt(
        issuer: String = actualIssuer,
        urlDecodedBody: String,
        overridingClaims: Map<String, Any> = emptyMap()
    ) : String {
        val parameters = urlDecodedBody.urlDecodedBodyAsMap()
        val subjectTokenClaims = parameters.getValue("subject_token").extractClaims()
        parameters.getValue("client_assertion").extractClaims().verifyClaims()

        val tokendingsClaims = overridingClaims.toMutableMap().apply {
            // https://doc.nais.io/security/auth/tokenx/#claims
            putIfAbsent("client_id", parameters.getValue("client_assertion").extractClientId())
            putIfAbsent("aud", parameters.getValue("audience")) // The intended audience for the token, must match your application's client_id.
            subjectTokenClaims["sub"]?.also { subject ->
                putIfAbsent("sub", subject) // If applicable, used in user centric access control. This represents a unique identifier for the user.
            }
            putIfAbsent("iss", issuer)
        }

        return jwsFunctions.generateJwt(
            claims = subjectTokenClaims.plus(tokendingsClaims)
        )
    }

    fun generateUrlDecodedBody(
        grantType: String = "urn:ietf:params:oauth:grant-type:token-exchange",
        clientAssertionType: String = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
        clientId: String = "dev-fss:dusseldorf:k9-selvbetjening-oppslag",
        clientAssertion: String = generateAssertionJwt(mapOf("client_id" to clientId)),
        subjectTokenType: String = "urn:ietf:params:oauth:token-type:jwt",
        subjectToken: String
    ): String = "grant_type=$grantType&client_assertion_type=$clientAssertionType&client_assertion=$clientAssertion&subject_token_type=$subjectTokenType&subject_token=$subjectToken&audience=${getAudience()}"

    fun generateAssertionJwt(claims: Map<String, Any>): String {
        return jwsFunctions.generateJwt(
            claims = claims
        )
    }

    private fun MutableMap<String, Any>.verifyClaims() = apply {
        check(contains("iss"))
        check(contains("aud"))
        check(contains("sub"))
        check(contains("iat"))
        check(contains("nbf"))
        check(contains("exp"))
        check(contains("jti"))
    }

    private fun String.extractClaims() = SignedJWT.parse(this).jwtClaimsSet.claims
    private fun String.extractClientId() = SignedJWT.parse(this).jwtClaimsSet.issuer
}
