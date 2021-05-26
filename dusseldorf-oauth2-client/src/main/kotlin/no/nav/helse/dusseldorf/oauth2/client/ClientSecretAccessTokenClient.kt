package no.nav.helse.dusseldorf.oauth2.client

import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant
import com.nimbusds.oauth2.sdk.JWTBearerGrant
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import java.net.URI

class ClientSecretAccessTokenClient(
        clientId : String,
        clientSecret: String,
        authenticationMode: AuthenticationMode = AuthenticationMode.POST,
        private val tokenEndpoint : URI
) : AccessTokenClient, NimbusAccessTokenClient() {

    private val clientAuthentication = when (authenticationMode) {
        AuthenticationMode.POST ->  ClientSecretPost(
            ClientID(clientId),
            Secret(clientSecret)
        )
        AuthenticationMode.BASIC -> ClientSecretBasic(
            ClientID(clientId),
            Secret(clientSecret)
        )
    }

    override fun getAccessToken(
            scopes: Set<String>,
            onBehalfOf: String
    ): AccessTokenResponse = getAccessToken(getOnBehalfOfTokenRequest(onBehalfOf, scopes))

    override fun getAccessToken(
            scopes: Set<String>
    ): AccessTokenResponse = getAccessToken(getClientCredentialsTokenRequest(scopes))

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow
    private fun getClientCredentialsTokenRequest(
            scopes: Set<String>
    ) : TokenRequest = TokenRequest(
            tokenEndpoint,
            clientAuthentication,
            ClientCredentialsGrant(),
            getScope(scopes)
    )

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow
    private fun getOnBehalfOfTokenRequest(
            onBehalfOf: String,
            scopes: Set<String>
    ) : TokenRequest = TokenRequest(
            tokenEndpoint,
            clientAuthentication,
            JWTBearerGrant(SignedJWT.parse(onBehalfOf)),
            getScope(scopes),
            null,
            onBehalfOfParameters
    )

    enum class AuthenticationMode(private val rfc: String) {
        BASIC("client_secret_basic"),
        POST("client_secret_post")
    }
}