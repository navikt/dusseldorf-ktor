package no.nav.helse.dusseldorf.ktor.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import com.nimbusds.jwt.SignedJWT
import no.nav.helse.dusseldorf.ktor.testsupport.jws.AnyIssuer
import java.util.*

internal class NaisStsTokenResponseTransformer(
        private val name: String,
        private val issuer: String
) : ResponseTransformer() {

    private val naisStsIssuer = AnyIssuer(issuer)

    override fun transform(
            request: Request?,
            response: Response?,
            files: FileSource?,
            parameters: Parameters?
    ): Response {
        val encodedCredentials = request!!.getHeader("Authorization").substringAfter("Basic ")
        val subject = String(Base64.getDecoder().decode(encodedCredentials)).substringBefore(":")

        val accessToken = naisStsIssuer.generateJwt(claims = mapOf("sub" to subject))

        val expiresIn = (SignedJWT.parse(accessToken).jwtClaimsSet.expirationTime.time - Date().time) / 1000
        return Response.Builder.like(response)
                .status(200)
                .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json")))
                .body("""
                    {
                        "access_token" : "$accessToken",
                        "expires_in" : $expiresIn,
                        "token_type": "Bearer"
                    }
                """.trimIndent())
                .build()
    }

    override fun getName() = name
    override fun applyGlobally() = false
}