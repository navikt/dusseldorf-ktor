package no.nav.helse.dusseldorf.ktor.auth

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
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
                (JSONParser().parse(privateKeyJwk) as JSONObject)["kid"].toString()
        )
) : Client(clientId, tokenEndpoint)