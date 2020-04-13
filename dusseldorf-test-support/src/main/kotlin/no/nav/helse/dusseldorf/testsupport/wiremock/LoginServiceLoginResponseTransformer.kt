package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.helse.dusseldorf.testsupport.http.LoginRequest
import no.nav.helse.dusseldorf.testsupport.http.LoginServiceLogin
import java.net.URI

internal class LoginServiceLoginResponseTransformer(
        private val name: String
) : ResponseTransformer() {
    override fun transform(
            request: Request?,
            response: Response?,
            files: FileSource?,
            parameters: Parameters?
    ): Response {

        val loginRequest = WiremockLoginRequest(request!!)
        val loginResponse = LoginServiceLogin.login(loginRequest)

        val cookie = Cookie(listOfNotNull(
                "${loginResponse.cookie.name}=${loginResponse.cookie.value}",
                "Path=${loginResponse.cookie.path}",
                "Domain=${loginResponse.cookie.domain}",
                if (loginResponse.cookie.secure) "Secure=true" else null,
                "HttpOnly=${loginResponse.cookie.httpOnly}"
        ))

        return Response.Builder.like(response)
                .headers(HttpHeaders(
                        HttpHeader.httpHeader("Location", loginResponse.location.toString()),
                        HttpHeader.httpHeader("Set-Cookie", cookie.toString())
                ))
                .status(302)
                .body(cookie.toString())
                .build()
    }

    override fun getName() = name
    override fun applyGlobally() = false
}

private data class WiremockLoginRequest(val request: Request) : LoginRequest {
    private val redirect = URI.create(request.queryParameter("redirect").firstValue())
    private val cookieName = request.queryParameter("cookieName")
    private val fnr = request.queryParameter("fnr")
    private val level = request.queryParameter("level")

    override fun level(): Int? = if (level.isPresent) level.firstValue().toInt() else null
    override fun fnr(): String? = if (fnr.isPresent) fnr.firstValue() else null
    override fun redirect(): URI = redirect
    override fun cookieName(): String? = if (cookieName.isPresent) cookieName.firstValue() else null

}