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

private fun LoginRequest.levelOrFallback() : Int = if (level() != null) level()!! else 4
private fun LoginRequest.fnrOrFallback() : String = if (fnr() != null) fnr()!! else "17420373147"
private fun LoginRequest.cookieNameOrFallback() : String = if (cookieName() != null) cookieName()!! else "localhost-idtoken"
