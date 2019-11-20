package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.helse.dusseldorf.testsupport.jws.NaisSts
import java.util.*

internal class NaisStsTokenResponseTransformer(
        private val name: String
) : ResponseTransformer() {

    override fun transform(
            request: Request?,
            response: Response?,
            files: FileSource?,
            parameters: Parameters?
    ): Response {
        val encodedCredentials = request!!.getHeader("Authorization").substringAfter("Basic ")
        val subject = String(Base64.getDecoder().decode(encodedCredentials)).substringBefore(":")
        val accessToken = NaisSts.generateJwt(application = subject)

        return Response.Builder.like(response)
                .status(200)
                .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json")))
                .body("""
                    {
                        "access_token" : "$accessToken",
                        "expires_in" : ${accessToken.getExpiresIn()},
                        "token_type": "Bearer"
                    }
                """.trimIndent())
                .build()
    }

    override fun getName() = name
    override fun applyGlobally() = false
}