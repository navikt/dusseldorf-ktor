package no.nav.helse.dusseldorf.ktor.auth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.net.URI

abstract class Client(private val clientId : String, private val tokenEndpoint: URI) {
    fun clientId() : String = clientId
    fun tokenEndpoint() : URI = tokenEndpoint
}
data class ClientSecretClient(
        private val clientId: String,
        private val tokenEndpoint: URI,
        val clientSecret: String
) : Client(clientId, tokenEndpoint)

data class PrivateKeyClient(
        private val clientId: String,
        private val tokenEndpoint: URI,
        val privateKeyJwk: String,
        val certificateHexThumbprint : String = requireNotNull(
                (Json.parseToJsonElement(privateKeyJwk).jsonObject)["kid"].toString()
        )
) : Client(clientId, tokenEndpoint)