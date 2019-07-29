package no.nav.helse.dusseldorf.ktor.testsupport.jws

object Azure {
    enum class ClientAuthenticationMode(val claimValue: String) {
        PUBLIC("0"),
        CLIENT_SECRET("1"),
        CERTIFICATE("2")
    }

    object V1_0 : Issuer {
        private const val version = "1.0"
        private const val issuer = "http://localhost/azure/v1.0"

        override fun getIssuer() = issuer
        override fun getPublicJwk() = JwsFunctions.getPublicJwk()

        fun generateJwt(
                clientId: String,
                audience: String,
                clientAuthenticationMode: ClientAuthenticationMode = Azure.ClientAuthenticationMode.CERTIFICATE,
                groups: Set<String> = emptySet(),
                roles: Set<String> = emptySet(),
                scopes: Set<String> = emptySet()
        ) = JwsFunctions.generateJwt(
                claims = mapOf(
                        "ver" to version,
                        "aud" to audience,
                        "iss" to issuer,
                        "appid" to clientId,
                        "appidacr" to clientAuthenticationMode.claimValue,
                        "groups" to groups,
                        "roles" to roles,
                        "scp" to scopes.joinToString(" ")
                )
        )
    }

    object V2_0 : Issuer {
        private const val version = "2.0"
        private const val issuer = "http://localhost/azure/v2.0"

        override fun getIssuer() = issuer
        override fun getPublicJwk() = JwsFunctions.getPublicJwk()

        fun generateJwt(
                clientId: String,
                audience: String,
                clientAuthenticationMode: ClientAuthenticationMode = Azure.ClientAuthenticationMode.CERTIFICATE,
                groups: Set<String> = emptySet(),
                roles: Set<String> = emptySet(),
                scopes: Set<String> = emptySet()
        ) = JwsFunctions.generateJwt(
                claims = mapOf(
                        "ver" to version,
                        "aud" to audience,
                        "iss" to issuer,
                        "azp" to clientId,
                        "azpacr" to clientAuthenticationMode.claimValue,
                        "groups" to groups,
                        "roles" to roles,
                        "scp" to scopes.joinToString(" ")
                )
        )
    }
}