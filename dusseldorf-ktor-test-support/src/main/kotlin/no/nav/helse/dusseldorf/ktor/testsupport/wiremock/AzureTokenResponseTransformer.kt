package no.nav.helse.dusseldorf.ktor.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import com.nimbusds.jwt.SignedJWT
import java.net.URLDecoder
import java.util.*
import kotlin.IllegalStateException

internal class AzureTokenResponseTransformer(
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
        val body = URLDecoder.decode(request!!.bodyAsString,"UTF-8")
        val clientId = getParameter("client_id", body)
        val scopes = getScopes(body)
        val audience = extractAudience(scopes)
        val accessToken = accessTokenGenerator(clientId, audience, scopes)
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

    private fun getParameter(parameterName: String, urlDecodedBody: String) : String {
        if (!urlDecodedBody.contains("$parameterName=")) throw IllegalStateException("Parameter $parameterName ikke funnet i request $urlDecodedBody")
        val afterParamName = urlDecodedBody.substringAfter("$parameterName=")
        return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
        else afterParamName
    }
    private fun getScopes(urlDecodedBody: String) = getParameter("scope", urlDecodedBody).split( " ").toSet()
    private fun extractAudience(scopes: Set<String>) = scopes.first { it.endsWith("/.default") }.substringBefore("/.default")
}


