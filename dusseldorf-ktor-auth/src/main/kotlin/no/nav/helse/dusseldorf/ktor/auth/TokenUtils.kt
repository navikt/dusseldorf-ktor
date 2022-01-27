package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwt.JWT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.auth.*

data class IdToken(val value: String) {
    private val jwt = try {
        JWT.decode(value)
    } catch (cause: Throwable) {
        throw IdTokenInvalidFormatException(this, cause)
    }

    fun getNorskIdentifikasjonsnummer(): String {
        return when {
            issuerIsLoginservice() -> jwt.claims["sub"]?.asString() ?: throw IllegalStateException("Loginservice token mangler 'sub' claim.")
            issuerIsIDPorten() -> jwt.claims["pid"]?.asString() ?: throw IllegalStateException("IDPorten token mangler 'pid' claim.")
            issuerIsTokendings() -> jwt.claims["pid"]?.asString() ?: jwt.claims["sub"]?.asString() ?: throw IllegalStateException("Tokendings token mangler 'pid/sub' claim.")
            else -> throw IllegalStateException("${jwt.issuer} er ukjent.")
        }
    }

    fun getId(): String? = jwt.id
    fun issuer() = jwt.issuer.lowercase()
    fun issuerIsLoginservice() = issuer().contains("b2clogin") || issuer().contains("login-service")
    fun issuerIsIDPorten() = issuer().contains("idporten")
    fun issuerIsTokendings() = issuer().contains("tokendings")
    fun issuerIsAzure() = issuer().contains("microsoftonline") || issuer().contains("azure")
    fun somHttpAuthHeader(): HttpAuthHeader = HttpAuthHeader.Single("Bearer", value)
}

class IdTokenProvider(
    private val cookieName : String?
) {
    fun getIdToken(call: ApplicationCall) : IdToken {
        val jwt = call.request.parseAuthorizationHeader()?.render()
        if(jwt != null) return IdToken(jwt.substringAfter("Bearer "))

        if(cookieName != null) {
            val cookie = call.request.cookies[cookieName]
            if(cookie != null) return IdToken(cookie)
            else throw CookieNotSetException(cookieName)
        }

        throw UnAuthorizedException()
    }
}

fun ApplicationCall.idToken() : IdToken {
    val jwt: String = request.parseAuthorizationHeader()?.render() ?: throw IllegalStateException("Token ikke satt")
    return IdToken(jwt.substringAfter("Bearer "))
}

class CookieNotSetException(cookieName : String) : RuntimeException("Ingen cookie med navnet '$cookieName' satt.")
class IdTokenInvalidFormatException(idToken: IdToken, cause: Throwable? = null) : RuntimeException("$idToken er på ugyldig format.", cause)
class UnAuthorizedException() : RuntimeException("Fant ikke token som cookie eller authorization header.")
