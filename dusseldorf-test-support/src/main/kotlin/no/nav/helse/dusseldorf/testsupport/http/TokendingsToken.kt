package no.nav.helse.dusseldorf.testsupport.http

import no.nav.helse.dusseldorf.testsupport.jws.Tokendings
import no.nav.helse.dusseldorf.testsupport.wiremock.getExpiresIn

object TokendingsToken {
    fun response(
        request: TokendingsTokenRequest,
        issuer: String) : String {

        val accessToken = Tokendings.generateJwt(
            issuer = issuer,
            urlDecodedBody = request.urlDecodedBody()
        )

        return """
            {
                "token_type": "Bearer",
                "access_token" : "$accessToken",
                "issued_token_type" : "urn:ietf:params:oauth:token-type:access_token",
                "expires_in" : ${accessToken.getExpiresIn()}
            }
        """.trimIndent()
    }
}

interface TokendingsTokenRequest {
    fun urlDecodedBody(): String
}