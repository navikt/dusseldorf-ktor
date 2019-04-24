package no.nav.helse.dusseldorf.ktor.auth

import java.net.URL

abstract class Client(private val clientId : String, private val tokenEndpoint: URL) {
    fun clientId() : String = clientId
    fun tokenEndpoint() : URL = tokenEndpoint
}
data class ClientSecretClient(private val clientId: String, private val tokenEndpoint: URL, val clientSecret: String) : Client(clientId, tokenEndpoint)
data class PrivateKeyClient(private val clientId: String, private val tokenEndpoint: URL, val privateKeyJwk: String, val certificateHexThumbprint : String) : Client(clientId, tokenEndpoint)