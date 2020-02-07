package no.nav.helse.dusseldorf.testsupport.http

import no.nav.helse.dusseldorf.testsupport.jws.NaisSts
import no.nav.helse.dusseldorf.testsupport.wiremock.getExpiresIn
import java.util.*

object NaisStsToken {
    fun response(
            request: NaisStsTokenRequest,
            issuer: String) : String {

        val accessToken = NaisSts.generateJwt(
            issuer = issuer,
            application = request.clientId()
        )

        return """
            {
                "token_type": "Bearer",
                "access_token" : "$accessToken",
                "expires_in" : ${accessToken.getExpiresIn()}
            }
        """.trimIndent()
    }

}

interface NaisStsTokenRequest {
    fun authorizationHeader() : String
}

private fun NaisStsTokenRequest.clientId() : String {
    val encodedCredentials = authorizationHeader().substringAfter("Basic ")
    return String(Base64.getDecoder().decode(encodedCredentials)).substringBefore(":")
}