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

    fun somHttpAuthHeader(): HttpAuthHeader = HttpAuthHeader.Single("Bearer", value)

    fun getId(): String? = jwt.id
    fun getNorskIdentifikasjonsnummer(): String {
        val issuer = jwt.issuer.lowercase()
        return when {
            issuer.contains("b2clogin") || issuer.contains("login-service") -> jwt.claims["sub"]?.asString() ?: throw IllegalStateException("Token mangler 'sub' claim.")
            issuer.contains("idporten") -> jwt.claims["pid"]?.asString() ?: throw IllegalStateException("Token mangler 'pid' claim.")
            else -> throw IllegalStateException("${jwt.issuer} er ukjent.")
        }
    }
}

class IdTokenProvider(
    private val cookieName : String
) {
    fun getIdToken(call: ApplicationCall) : IdToken {
        val cookie = call.request.cookies[cookieName] ?: throw CookieNotSetException(cookieName)
        return IdToken(value = cookie)
    }
}

fun ApplicationCall.idToken() : IdToken {
    val jwt: String = request.parseAuthorizationHeader()?.render() ?: throw IllegalStateException("Token ikke satt")
    return IdToken(jwt.substringAfter("Bearer "))
}

class CookieNotSetException(cookieName : String) : RuntimeException("Ingen cookie med navnet '$cookieName' satt.")
class IdTokenInvalidFormatException(idToken: IdToken, cause: Throwable? = null) : RuntimeException("$idToken er på ugyldig format.", cause)
