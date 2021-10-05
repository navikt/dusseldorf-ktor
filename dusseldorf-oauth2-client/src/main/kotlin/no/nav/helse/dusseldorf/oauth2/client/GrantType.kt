package no.nav.helse.dusseldorf.oauth2.client

import java.net.URI

enum class GrantType {
    JwtBearer,
    TokenExchange;

    internal companion object {
        internal fun URI.grantType() = when ("$this".contains("tonkendings")) {
            true -> TokenExchange
            false -> JwtBearer
        }
    }
}