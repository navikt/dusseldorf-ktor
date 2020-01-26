package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import no.nav.helse.dusseldorf.testsupport.http.AzureToken
import no.nav.helse.dusseldorf.testsupport.http.TokenRequest
import java.net.URLDecoder

internal class AzureTokenResponseTransformer(
        private val name: String
) : ResponseTransformer() {

    override fun getName() = name
    override fun applyGlobally() = false

    override fun transform(
            request: Request?,
            response: Response?,
            files: FileSource?,
            parameters: Parameters?
    ): Response {
        val tokenResponse = AzureToken.response(
                request = WireMockTokenRequest(request!!)
        )

        return Response.Builder.like(response)
                .status(200)
                .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json; charset=UTF-8")))
                .body(tokenResponse)
                .build()
    }
}

internal class WireMockTokenRequest(request: Request) : TokenRequest {
    private val body = URLDecoder.decode(request.bodyAsString, Charsets.UTF_8)!!
    private val path = request.absoluteUrl!!
    private val authorizationHeader = request.getHeader("Authorization")
    override fun urlDecodedBody() = body
    override fun path() = path
    override fun authorizationHeader() : String? = authorizationHeader
}