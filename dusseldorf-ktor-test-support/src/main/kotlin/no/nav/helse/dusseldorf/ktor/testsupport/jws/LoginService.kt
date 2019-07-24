package no.nav.helse.dusseldorf.ktor.testsupport.jws

import java.time.LocalDateTime
import java.util.*

object LoginService {
    private const val audienceTemplate = "http://localhost:8080/loginservice/audience/v"
    private const val issuerTemplate = "http://localhost:8080/loginservice/issuer/v"

    object V1_0 : Issuer {
        private const val version = "1.0"
        private const val audience = "$audienceTemplate$version"
        private const val issuer = "$issuerTemplate$version"

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