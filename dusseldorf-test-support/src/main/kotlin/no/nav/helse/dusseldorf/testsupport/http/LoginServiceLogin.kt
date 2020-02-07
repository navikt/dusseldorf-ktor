package no.nav.helse.dusseldorf.testsupport.http

import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import java.net.URI

object LoginServiceLogin {
    fun login(request: LoginRequest) : LoginResponse {
        val idToken = LoginService.V1_0.generateJwt(
                level = request.levelOrFallback(),
                fnr = request.fnrOrFallback()
        )
        return LoginResponse(
                location = request.redirect(),
                cookie = Cookie(
                        name = request.cookieNameOrFallback(),
                        value = idToken,
                        domain = request.redirect().host,
                        secure = request.cookieSecure()
                )
        )
    }
}

data class LoginResponse(
        val location: URI,
        val cookie: Cookie
)

data class Cookie(
        val name: String,
        val value: String,
        val domain: String,
        val path: String = "/",
        val secure: Boolean,
        val httpOnly: Boolean = true
)

interface LoginRequest {
    fun level() : Int?
    fun fnr() : String?
    fun redirect() : URI
    fun cookieName(): String?
    fun cookieSecure(): Boolean = false
}

private fun LoginRequest.levelOrFallback() : Int = if (level() != null) level()!! else 4
private fun LoginRequest.fnrOrFallback() : String = if (fnr() != null) fnr()!! else "22046474256"
private fun LoginRequest.cookieNameOrFallback() : String = if (cookieName() != null) cookieName()!! else "localhost-idtoken"