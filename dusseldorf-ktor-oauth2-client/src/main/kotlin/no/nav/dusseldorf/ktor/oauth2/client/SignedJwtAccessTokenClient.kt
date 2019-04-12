package no.nav.dusseldorf.ktor.oauth2.client

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.net.URL
import java.time.*
import java.util.*

private val logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.oauth2.client.ClientAuthenticationPrivateKeyJwt")
private val onBehalfOfParameters = mapOf("requested_token_use" to listOf("on_behalf_of"))

class SignedJwtAccessTokenClient(
        private val clientId: String,
        clientPrivateKeyJwk: String,
        certificateKid: String,
        certificateKidTransformer: (String) -> String = {
            Base64.getUrlEncoder().encodeToString(Hex.decodeHex(it.toCharArray()))
        },
        private val tokenUrl: URL
) : AccessTokenClient {
    private val jwsSigner: JWSSigner
    private val algorithm : JWSAlgorithm = JWSAlgorithm.RS256
    private val privateKey : RSAKey = RSAKey.parse(clientPrivateKeyJwk)
    private val transformedKid : String
    private val jwsHeader : JWSHeader

    init {
        if (!privateKey.isPrivate) throw IllegalArgumentException("JWK er ikke en private key.")
        jwsSigner = RSASSASigner(privateKey)
        transformedKid = certificateKidTransformer(certificateKid)
        jwsHeader = JWSHeader.Builder(algorithm)
                .keyID(transformedKid)
                .type(JOSEObjectType.JWT)
                .build()
    }

    override fun getAccessToken(scopes: Set<String>) : AccessTokenResponse {
        return getAccessToken(getClientCredentialsTokenRequest(getScope(scopes)))
    }

    override fun getAccessToken(scopes: Set<String>, onBehalfOf: String) : AccessTokenResponse {
        return getAccessToken(getOnBehalfOfTokenRequest(onBehalfOf, getScope(scopes)))
    }

    private fun getScope(scopes: Set<String>) = if (scopes.isEmpty()) null else Scope.parse(scopes)

    private fun getAccessToken(
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

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow#second-case-access-token-request-with-a-certificate
    private fun getClientCredentialsTokenRequest(
            scope: Scope?
    ) : TokenRequest = TokenRequest(
            tokenUrl.toURI(),
            PrivateKeyJWT(getSignedJwt()),
            ClientCredentialsGrant(),
            scope
    )

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow#second-case-access-token-request-with-a-certificate
    private fun getOnBehalfOfTokenRequest(
            onBehalfOf: String,
            scope: Scope?
    ) : TokenRequest = TokenRequest(
            tokenUrl.toURI(),
            PrivateKeyJWT(getSignedJwt()),
            JWTBearerGrant(SignedJWT.parse(onBehalfOf)),
            scope,
            null,
            onBehalfOfParameters
    )

    private fun getSignedJwt() : SignedJWT {
        val jwtClaimSet = JWTClaimsSet.Builder()
                .audience(tokenUrl.toString())
                .subject(clientId)
                .issuer(clientId)
                .jwtID(UUID.randomUUID().toString())
                .notBeforeTime(getNotBeforeTime())
                .expirationTime(getExpirationTime())
                .build()

        val signedJwt = SignedJWT(jwsHeader, jwtClaimSet)
        signedJwt.sign(jwsSigner)
        return signedJwt
    }

    private fun getNotBeforeTime() : Date {
        val now = LocalDateTime.now(Clock.systemUTC())
        return Date.from(now.toInstant(ZoneOffset.UTC))
    }

    private fun getExpirationTime() : Date {
        val exp = LocalDateTime.now(Clock.systemUTC()).plusSeconds(10)
        return Date.from(exp.toInstant(ZoneOffset.UTC))
    }
}
