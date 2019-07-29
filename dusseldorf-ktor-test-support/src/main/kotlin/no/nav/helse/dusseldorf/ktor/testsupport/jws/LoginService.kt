package no.nav.helse.dusseldorf.ktor.testsupport.jws
import java.time.LocalDateTime
import java.util.*

object LoginService {
    object V1_0 : Issuer {
        private const val version = "1.0"
        private const val audience = "http://localhost/login-service/v1.0/audience"
        private const val issuer = "http://localhost/login-service/v1.0/issuer"

        override fun getIssuer() = issuer
        fun getAudience() = audience
        override fun getPublicJwk() = JwsFunctions.getPublicJwk()

        fun generateJwt(
                level: Int,
                fnr: String
        ) = JwsFunctions.generateJwt(
                claims = mapOf(
                        "acr" to "Level$level",
                        "sub" to fnr,
                        "aud" to audience,
                        "iss" to issuer,
                        "auth_time" to LocalDateTime.now().toDate(),
                        "ver" to version,
                        "nonce" to UUID.randomUUID().toString()
                )
        )
    }
}