package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.http.Request
import java.net.URLDecoder

internal object AzureTokenTransformerUtils {
    internal fun urlDecodedBody(request: Request) = URLDecoder.decode(request.bodyAsString,"UTF-8")
    internal fun getParameter(parameterName: String, urlDecodedBody: String) : String {
        check(urlDecodedBody.contains("$parameterName=")) { "Parameter $parameterName ikke funnet i request $urlDecodedBody" }
        val afterParamName = urlDecodedBody.substringAfter("$parameterName=")
        return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
        else afterParamName
    }
    internal fun getScopes(urlDecodedBody: String) = getParameter("scope", urlDecodedBody).split( " ").toSet()
    internal fun extractAudience(scopes: Set<String>) = scopes.first { it.endsWith("/.default") }.substringBefore("/.default")
}