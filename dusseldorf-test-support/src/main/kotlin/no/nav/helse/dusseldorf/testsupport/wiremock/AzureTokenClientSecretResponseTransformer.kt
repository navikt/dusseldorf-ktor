package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import java.net.URLDecoder
import java.util.*

internal class AzureTokenClientSecretResponseTransformer(
        private val name: String,
        private val accessTokenGenerator: (clientId: String, audience: String, scopes: Set<String>) -> String
) : ResponseTransformer() {

    override fun getName() = name
    override fun applyGlobally() = false

    override fun transform(
            request: Request?,
            response: Response?,
            files: FileSource?,
            parameters: Parameters?
    ): Response {
        val scopes = URLDecoder.decode(request!!.queryParameter("scope")
                .firstValue(),"UTF-8")
                .split(" ")
                .toSet()

        val clientId = String(Base64.getDecoder().decode(request.getHeader("Authorization"))).split(":")[0]

        val audience = extractAudience(scopes)

        val accessToken = accessTokenGenerator(clientId, audience, scopes)

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

    private fun extractAudience(scopes: Set<String>) = scopes.first { it.endsWith("/.default") }.substringBefore("/.default")
}