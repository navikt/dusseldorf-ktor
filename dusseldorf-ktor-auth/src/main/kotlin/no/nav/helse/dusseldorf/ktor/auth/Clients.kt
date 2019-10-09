package no.nav.helse.dusseldorf.ktor.auth

import java.net.URI

abstract class Client(private val clientId : String, private val tokenEndpoint: URI) {
    fun clientId() : String = clientId
    fun tokenEndpoint() : URI = tokenEndpoint
}
data class ClientSecretClient(private val clientId: String, private val tokenEndpoint: URI, val clientSecret: String) : Client(clientId, tokenEndpoint)
data class PrivateKeyClient(private val clientId: String, private val tokenEndpoint: URI, val privateKeyJwk: String, val certificateHexThumbprint : String) : Client(clientId, tokenEndpoint)
data class PrivateKeyClientV2(private val clientId: String, private val tokenEndpoint: URI, val privateKeyPem: String, val certificateBase64Thumbprint: String) : Client(clientId, tokenEndpoint)