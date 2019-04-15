package no.nav.dusseldorf.ktor.oauth2.client

import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.TokenResponse
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException

private val logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.oauth2.client.NimbusAccessTokenClient")
internal val onBehalfOfParameters = mapOf("requested_token_use" to listOf("on_behalf_of"))


abstract class NimbusAccessTokenClient {

    internal fun getAccessToken(
            tokenRequest: TokenRequest
    ) : AccessTokenResponse {
        val httpRequest = tokenRequest.toHTTPRequest()

        logger.trace("Requester URL='${httpRequest.url}?${httpRequest.query}'")

        val response = TokenResponse.parse(httpRequest.send())

        if (response.indicatesSuccess()) {
            val successResponse = response.toSuccessResponse()
            logger.trace("Mottok nytt access token = '$successResponse'")
            return AccessTokenResponse(
                    accessToken = successResponse.tokens.accessToken.value,
                    expiresIn = successResponse.tokens.accessToken.lifetime,
                    tokenType = successResponse.tokens.accessToken.type.value
            )
        }
        else {
            val errorResponse = response.toErrorResponse().toJSONObject()
            throw IllegalStateException("Feil ved henting av access token = '$errorResponse'")
        }
    }

    internal fun getScope(scopes: Set<String>) = if (scopes.isEmpty()) null else Scope.parse(scopes)

}