package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.helse.dusseldorf.testsupport.http.NaisStsToken
import no.nav.helse.dusseldorf.testsupport.http.NaisStsTokenRequest

internal class NaisStsTokenResponseTransformer(
        private val name: String,
        private val issuer: String
) : ResponseTransformer() {

    override fun transform(
            request: Request?,
            response: Response?,
            files: FileSource?,
            parameters: Parameters?
    ): Response {
        val tokenResponse = NaisStsToken.response(
                request = WireMockNaisStsTokenRequest(request!!),
                issuer = issuer
        )

        return Response.Builder.like(response)
                .status(200)
                .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json")))
                .body(tokenResponse)
                .build()
    }

    override fun getName() = name
    override fun applyGlobally() = false
}

private class WireMockNaisStsTokenRequest(request: Request) : NaisStsTokenRequest {
    private val authorizationHeader = request.getHeader("Authorization")
    override fun authorizationHeader(): String = authorizationHeader
}