package no.nav.helse.dusseldorf.ktor.testsupport.jws

object NaisSts : Issuer {
    private const val audience = "http://localhost/nais-sts/audience"
    private const val actualIssuer = "http://localhost/nais-sts/issuer"

    override fun getPublicJwk() = JwsFunctions.getPublicJwk()
    override fun getIssuer() = actualIssuer
    fun getAudience() = audience

    fun generateJwt(
            application: String,
            issuer: String = actualIssuer
    ) = JwsFunctions.generateJwt(
            claims = mapOf(
                    "sub" to application,
                    "aud" to audience,
                    "iss" to issuer
            )
    )
}