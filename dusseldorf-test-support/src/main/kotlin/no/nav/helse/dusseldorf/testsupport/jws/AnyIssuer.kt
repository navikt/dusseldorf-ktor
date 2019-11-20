package no.nav.helse.dusseldorf.testsupport.jws

class AnyIssuer(
        private val issuer: String,
        privateKeyJwk: String = defaultPrivateKeyJwk
) : Issuer {

    private companion object {
        private val defaultPrivateKeyJwk = """
        {
            "kid": "smCCaWOmV-KAhu5jrVl9jYdk2u0=",
            "kty": "RSA",
            "n": "6onP3-_Z25yXSDAoFHrjm-jsPRPCBJxMYLTYwWX2BUBfgIxTsxgVqf1B2jKPcKPjfaFp182z31xi9E0ZEsM04eJ5iJ9qvUu0ZJ3UlHGr4VDiaDHYXLVhfuEBsjcaXSzhyAcfaGVmWQAS_JzEwZdinXZFNFVkRpQ4-bTYkymdKGz0KOO9-1iU5ZN01Vpe9CvBt7MSJ0NrpdDc5YXY26REA1dU1VH-Q2YbFJrgMSTPBiiQMQd71LbxLwkQQKZc9ZHYX3Cgi7Eg5KCpnsVlWI8IttaSHxkj58RueLuRDePm_6-6X6hZ_BktY0SVSWVDsAsVVNyoc04NNh6BgWGWJmrvIw",
            "e": "AQAB",
            "d": "dK8dgX7Vt0G_rgVvW0kMRfvq1DQpOZv9D7vJfuZYMKKnINAvsBFSbeD2yzSOUm7m9hDBFMFdNMnE3WAiEkiV3a-L4WHWe9jXJZu0MCohZhaUnVv3FaCTockVy_FIJ4T1y0tn38ta0PqNd6oS5XGIeWhm47N8EPUbJ3hGq2hvrLUcDUMd3Mb33XBwg19FU8J_xogrrtk9MKJqT9CmgI8N4Eff8JJj8e4lQZB-hAq97goUzZPUYdo-z2_DdVJsKx7vJoJ72MMKlyg3ZZ8E1BEOxhXxLBzeENTIgYDYJmOdpUhfu3sd6fBbIqXEnUh9_xVbyzgPzGzKFA-fIkEw9CneIQ",
            "p": "_T12kMi1epRTGsMs0ZUQGZfa7iJo8Anx1V21mwz_xkfAiKJmDxlGwilmFER_lCqGmNoDbT9u-TfQ3_KEW9A1xBMpzM5mwGG9h3WDuqTX8w9QVdNKM8ebTr8MhtAO43s9MA1NJP1sDq0wbeagWKQ0KwhKrEFbwsLLCRZ65TFWn7M",
            "q": "7Rgr0bhbuiP2EZI7DSnIGKUR5yVL9Ad414IzKyZ3NzUcC3CPxMN8dyBA2Rxde4xzG_xdWmBvauAvtbfa35WIuJqfU7nIIfLSD0-Toi6x62VsoSwojeOqY6mf46Q12sTCZfxgBFSX_af6ZA3sqRi0ltUgKzjb6vQD8sLyKSVsOtE",
            "dp": "t9Nm_uCDRBDpZpLpNNXk0v81kAlwbVZ6suyMSJoeDPvG2igwt6MchxLwI6Q5i_92pvwS4rnaP5Rxzi7IP7GbZdKXVDE7y0joAK2gStsgWzLxmOuAgKfTqEzstevP8vdhykDNkB1Z1CmJ7y0rlkzUBazGL9Zd2rn2Eom7-iVfgRs",
            "dq": "LMMUesq4nqLtcRHPjkOmIGkBwUb8WP0UJnPRX0Mq3MRtIAfHTNmvZPo05AiD6bBxhwYVRdvRDTd4KnGko_OiVV_z1qXLkAU8_WEDuWnhc1S5tpAUs_0YjSpYIm09MalWHlUQd6DU9THChyNN2_rrFVL1eNOKSYh6OlntyDDlhxE",
            "qi": "TqX39sKugYJ201h_lU2_QMqMMBMD8idlfj19FQtYwpPPRAZkm4bJ1k6zlQqo4MK78qnUM09y5s5MEamIlk2KHHqcFYIUpjzQPXRZ95mkALYpoIeslsqwJAnbdHEoAqsKfvq6Zbk5Y7Cl9DmT5RJSWDFQYiPh77eWu43lFH-VVPk"
        }
        """.trimIndent()
    }

    private val jwsFunctions = JwsFunctions(privateKeyJwk)

    override fun getIssuer() = issuer
    override fun getPublicJwk() = jwsFunctions.getPublicJwk()

    fun generateJwt(
            headers: Map<String, Any> = emptyMap(),
            claims: Map<String, Any> = emptyMap()
    ) = jwsFunctions.generateJwt(
            headers = headers,
            claims = claims.toMutableMap().apply {
                if (!containsKey("iss")) put("iss", issuer)
            }.toMap()
    )
}