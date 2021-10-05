package no.nav.helse.dusseldorf.oauth2.client

import com.nimbusds.common.contenttype.ContentType
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.util.URLUtils
import java.net.URI

internal class TokenExchange(
    private val tokenEndpoint: URI,
    private val privateKeyJWT: PrivateKeyJWT,
    private val scope: Scope,
    private val onBehalfOf: SignedJWT) {

    internal fun toHTTPRequest(): HTTPRequest {
        val httpRequest = HTTPRequest(HTTPRequest.Method.POST, tokenEndpoint).apply {
            entityContentType = ContentType.APPLICATION_URLENCODED
            privateKeyJWT.applyTo(this)
        }

        val queryParameters = httpRequest.queryParameters.apply {
            put("grant_type", listOf("urn:ietf:params:oauth:grant-type:token-exchange"))
            put("subject_token_type", listOf("urn:ietf:params:oauth:client-assertion-type:jwt-bearer"))
            put("subject_token", listOf(onBehalfOf.serialize()))
            put("audience", listOf("$scope"))
        }

        httpRequest.query = URLUtils.serializeParameters(queryParameters)

        return httpRequest
    }
}