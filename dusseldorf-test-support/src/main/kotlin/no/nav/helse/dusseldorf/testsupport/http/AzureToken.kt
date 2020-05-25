package no.nav.helse.dusseldorf.testsupport.http

import com.nimbusds.jwt.SignedJWT
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.wiremock.getExpiresIn
import java.lang.IllegalStateException
import java.util.*

object AzureToken {
    fun response(
            request: TokenRequest,
            issuer: String) : String {
        return when {
            request.isClientCredentials() -> clientCredentials(request, issuer)
            request.isOnBehalfOf() -> onBehalfOf(request, issuer)
            request.isAuthorizationCode() -> authorizationCode(request, issuer)
            else -> throw IllegalStateException("Ikke støttet token operasjon.")
        }
    }

    private fun clientCredentials(
            request: TokenRequest,
            issuer: String) : String {
        val clientId = request.getClientId()
        val clientAuthenticationMode = request.clientAuthenticationMode()
        val scopes = request.getScopes()
        val audience = scopes.extractAudience()

        val accessToken = if (issuer.isV2()) Azure.V2_0.generateJwt(
                issuer = issuer,
                clientId = clientId,
                clientAuthenticationMode = clientAuthenticationMode,
                scopes = scopes,
                audience = audience
        ) else {
            Azure.V1_0.generateJwt(
                    issuer = issuer,
                    clientId = clientId,
                    clientAuthenticationMode = clientAuthenticationMode,
                    scopes = scopes,
                    audience = audience
            )
        }

        return """
            {
                "token_type": "Bearer",
                "access_token" : "$accessToken",
                "expires_in" : ${accessToken.getExpiresIn()}
            }
        """.trimIndent()
    }

    private fun onBehalfOf(
            request: TokenRequest,
            issuer: String) : String {
        val clientId = request.getClientId()
        val clientAuthenticationMode = request.clientAuthenticationMode()
        val scopes = request.getScopes()
        val audience = scopes.extractAudience()
        val name = request.getAssertion().jwtClaimsSet.getStringClaim("name")

        val accessToken = if (issuer.isV2()) Azure.V2_0.generateJwt(
                issuer = issuer,
                clientId = clientId,
                clientAuthenticationMode = clientAuthenticationMode,
                scopes = scopes,
                audience = audience,
                overridingClaims = mapOf(
                        "name" to name
                ),
                accessAsApplication = false
        ) else {
            Azure.V1_0.generateJwt(
                issuer = issuer,
                clientId = clientId,
                clientAuthenticationMode = clientAuthenticationMode,
                scopes = scopes,
                audience = audience,
                overridingClaims = mapOf(
                        "name" to name
                ),
                accessAsApplication = false
            )
        }

        return """
            {
                "token_type": "Bearer",
                "access_token" : "$accessToken",
                "expires_in" : ${accessToken.getExpiresIn()}
            }
        """.trimIndent()
    }

    private fun authorizationCode(
            request: TokenRequest,
            issuer: String) : String {
        val clientId = request.getClientId()
        val clientAuthenticationMode = request.clientAuthenticationMode()
        val code = Code.fraString(request.getCode())
        val scopes = code.scopes()
        val audience = scopes.extractAudience()

        val accessToken = if (issuer.isV2()) Azure.V2_0.generateJwt(
                issuer = issuer,
                clientId = clientId,
                clientAuthenticationMode = clientAuthenticationMode,
                scopes = scopes,
                audience = audience,
                overridingClaims = mapOf(
                        "name" to code.name
                ),
                accessAsApplication = false
        ) else {
            Azure.V1_0.generateJwt(
                issuer = issuer,
                clientId = clientId,
                clientAuthenticationMode = clientAuthenticationMode,
                scopes = scopes,
                audience = audience,
                overridingClaims = mapOf(
                        "name" to code.name
                ),
                accessAsApplication = false
            )
        }

        val idToken = if (issuer.isV2()) Azure.V2_0.generateJwt(
                issuer = issuer,
                clientId = clientId,
                audience = audience,
                scopes = scopes,
                overridingClaims = mapOf(
                        "name" to code.name,
                        "nonce" to code.nonce,
                        "sub" to code.userId
                ),
                accessAsApplication = false
        ) else {
            Azure.V1_0.generateJwt(
                issuer = issuer,
                clientId = clientId,
                audience = audience,
                scopes = scopes,
                overridingClaims = mapOf(
                        "name" to code.name,
                        "nonce" to code.nonce,
                        "sub" to code.userId
                ),
                accessAsApplication = false
            )
        }

        return """
            {
                "token_type": "Bearer",
                "access_token" : "$accessToken",
                "expires_in" : ${accessToken.getExpiresIn()},
                "id_token": "$idToken"
            }
        """.trimIndent()
    }

    data class Code(
            internal val userId: String,
            internal val name: String,
            internal val nonce: String,
            private val scopes: String) {
        companion object {
            internal fun fraString(string: String) : Code {
                val split = String(Base64.getUrlDecoder().decode(string)).split(";")
                return Code(
                        userId = split[0],
                        name = split[1],
                        scopes = split[2],
                        nonce = split[3]
                )
            }
            private fun String.utenSemikolon() = replace(";","_")
        }
        override fun toString() : String {
            val string =
                    "${userId.utenSemikolon()};" +
                    "${name.utenSemikolon()};" +
                    "${scopes.utenSemikolon()};" +
                    nonce.utenSemikolon()
            return Base64.getUrlEncoder().encodeToString(string.toByteArray())
        }
        internal fun scopes() = scopes.asScopes()
    }
}

interface TokenRequest {
    fun urlDecodedBody(): String
    fun authorizationHeader() : String?
}

private fun String.asScopes() = split(" ").toSet()
private fun Set<String>.extractAudience() = first { it.endsWith("/.default") }.substringBefore("/.default")

private fun String.getOptionalParameter(parameterName: String) : String? {
    if (!contains("$parameterName=")) return null
    val afterParamName = substringAfter("$parameterName=")
    return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
    else afterParamName
}
private fun String.getRequiredParameter(parameterName: String) : String {
    check(contains("$parameterName=")) { "Parameter $parameterName ikke funnet i request $this" }
    val afterParamName = substringAfter("$parameterName=")
    return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
    else afterParamName
}

private fun String.isV2() = contains("v2.0")
private fun TokenRequest.isClientCredentials() = urlDecodedBody().getOptionalParameter("grant_type")?.equals("client_credentials")?:false
private fun TokenRequest.isOnBehalfOf() = urlDecodedBody().getOptionalParameter("requested_token_use")?.equals("on_behalf_of")?:false
private fun TokenRequest.isAuthorizationCode() = urlDecodedBody().getOptionalParameter("grant_type")?.equals("authorization_code")?:false

private fun TokenRequest.getAssertion() = SignedJWT.parse(urlDecodedBody().getRequiredParameter("assertion"))
private fun TokenRequest.getScopes() = urlDecodedBody().getRequiredParameter("scope").asScopes()
private fun TokenRequest.getCode() = urlDecodedBody().getRequiredParameter("code")
private fun TokenRequest.getClientId() : String {
    val clientIdFraParameter = urlDecodedBody().getOptionalParameter("client_id")
    if (clientIdFraParameter != null) return clientIdFraParameter
    val clientAssertion = urlDecodedBody().getOptionalParameter("client_assertion")
    if (clientAssertion != null) return SignedJWT.parse(clientAssertion).jwtClaimsSet.issuer
    val credentials = authorizationHeader()!!.substringAfter("Basic ")
    return String(Base64.getDecoder().decode(credentials)).split(":")[0]
}
private fun TokenRequest.clientAuthenticationMode() : Azure.ClientAuthenticationMode {
    val clientAssertion = urlDecodedBody().getOptionalParameter("client_assertion")
    return if (clientAssertion != null) Azure.ClientAuthenticationMode.CERTIFICATE else Azure.ClientAuthenticationMode.CLIENT_SECRET
}