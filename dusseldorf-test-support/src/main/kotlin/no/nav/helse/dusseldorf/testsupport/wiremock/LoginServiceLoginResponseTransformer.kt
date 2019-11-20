package no.nav.helse.dusseldorf.testsupport.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.helse.dusseldorf.testsupport.jws.LoginService

internal class LoginServiceLoginResponseTransformer(
        private val name: String,
        private val cookieName : String
) : ResponseTransformer() {
    override fun transform(
            request: Request?,
            response: Response?,
            files: FileSource?,
            parameters: Parameters?
    ): Response {
        val redirectLocation = request!!.queryParameter("redirect").firstValue()
        val fnr = request.queryParameter("fnr").firstValue()
        val level = if (request.queryParameter("level").isPresent) request.queryParameter("level").firstValue().toInt() else 4

        val idToken = LoginService.V1_0.generateJwt(
                level = level,
                fnr = fnr
        )

        val cookie = Cookie(listOf(String.format("%s=%s", cookieName, idToken), "Path=/", "Domain=localhost"))

        return Response.Builder.like(response)
                .headers(HttpHeaders(
                        HttpHeader.httpHeader("Location", redirectLocation),
                        HttpHeader.httpHeader("Set-Cookie", cookie.toString())
                ))
                .status(302)
                .body(cookie.toString())
                .build()
    }

    override fun getName() = name
    override fun applyGlobally() = false
}