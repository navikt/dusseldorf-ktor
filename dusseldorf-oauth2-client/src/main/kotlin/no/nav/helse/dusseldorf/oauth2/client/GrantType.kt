package no.nav.helse.dusseldorf.oauth2.client

import java.net.URI

enum class GrantType {
    JwtBearer,
    TokenExchange;

    internal companion object {
        internal fun URI.grantType(): GrantType {
            val erTokenX = "$this".contains("tokenx") || "$this".contains("tokendings")
            return when (erTokenX) {
                true -> TokenExchange
                false -> JwtBearer
            }
        }
    }
}
