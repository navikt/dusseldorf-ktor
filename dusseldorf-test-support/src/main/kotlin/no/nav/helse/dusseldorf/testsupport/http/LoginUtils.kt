package no.nav.helse.dusseldorf.testsupport.http

import java.net.URI

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
    fun cookieSecure(): Boolean = redirect().toString().lowercase().startsWith("https")
}
