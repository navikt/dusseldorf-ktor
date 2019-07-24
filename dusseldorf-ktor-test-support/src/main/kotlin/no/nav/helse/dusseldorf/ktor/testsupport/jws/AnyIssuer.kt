package no.nav.helse.dusseldorf.ktor.testsupport.jws

class AnyIssuer(
        private val issuer: String
) : Issuer {
    override fun getIssuer() = issuer
    override fun getPublicJwk() = JwsFunctions.getPublicJwk()

    fun generateJwt(
            headers: Map<String, Any> = emptyMap(),
            claims: Map<String, Any> = emptyMap()
    ) = JwsFunctions.generateJwt(
            headers = headers,
            claims = claims.toMutableMap().apply {
                put("iss", issuer)
            }.toMap()
    )
}