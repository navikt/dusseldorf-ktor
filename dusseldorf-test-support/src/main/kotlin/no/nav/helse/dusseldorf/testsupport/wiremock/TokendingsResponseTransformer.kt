package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import no.nav.helse.dusseldorf.testsupport.http.TokendingsToken
import no.nav.helse.dusseldorf.testsupport.http.TokendingsTokenRequest
import java.net.URLDecoder

internal class TokendingsResponseTransformer(
    private val name: String,
    private val issuer: String
) : ResponseTransformer() {

    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val tokenResponse = TokendingsToken.response(
            request = WireMockTonkendingsTokenRequest(request!!),
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

private class WireMockTonkendingsTokenRequest(private val request: Request) : TokendingsTokenRequest {
    override fun urlDecodedBody() = URLDecoder.decode(request.bodyAsString, Charsets.UTF_8)!!
}