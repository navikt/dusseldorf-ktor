package no.nav.helse.dusseldorf.ktor.auth

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

private const val AZURE_TYPE = "azure"
private const val ISSUER = "issuer"
private const val JWKS_URI = "jwks_uri"
private const val TOKEN_ENDPOINT = "token_endpoint"

private val jsonParser = JSONParser()
private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.auth.AuthConfig")

@KtorExperimentalAPI
fun ApplicationConfig.issuers(path: String = "nav.auth.issuers") : Map<String, Issuer> {
    val issuersConfigList = configList(path)
    if (issuersConfigList.isNullOrEmpty()) return emptyMap()
    val issuers = mutableMapOf<String, Issuer>()
    for (issuerConfig in issuersConfigList) {
        // Required
        val alias = issuerConfig.getRequiredString("alias", false)
        logger.info("Issuer[$alias]")
        // Enten issuer+jwks_uri eller discovery_endpoint
        val discoveryJson = runBlocking { issuerConfig.getOptionalString("discovery_endpoint", false)?.discover(listOf(ISSUER, JWKS_URI)) }
        val issuer = if (discoveryJson != null) discoveryJson[ISSUER] as String else issuerConfig.getOptionalString(ISSUER, false)
        val jwksUrl = if (discoveryJson != null) discoveryJson[JWKS_URI] as String else issuerConfig.getOptionalString(JWKS_URI, false)
        logger.info("Issuer[$alias].issuer = '$issuer'")
        logger.info("Issuer[$alias].jwks_uri =  '$jwksUrl'")
        if (issuer == null || jwksUrl == null) {
            logger.info("Issuer[$alias] ikke konfigurert.")
            continue
        } else {
            logger.info("Issuer[$alias] er konfigurert.")
        }
        // Optional
        val type = issuerConfig.getOptionalString("type", false)
        val audience = issuerConfig.getOptionalString("audience", false)
        // Resolve issuer
        val resolvedIssuer = if (AZURE_TYPE.equals(type, false)) {
            if (audience == null) throw IllegalStateException("'audience' må settes for en issuer med type='azure'")
            val authorizedClient = issuerConfig.getOptionalList(key = "azure.authorized_clients", secret = false , builder = { value -> value }).toSet()
            val requiredGroups = issuerConfig.getOptionalList(key = "azure.required_groups", secret = false , builder = { value -> value }).toSet()
            val requiredRoles = issuerConfig.getOptionalList(key = "azure.required_roles", secret = false , builder = { value -> value }).toSet()
            val requireCertificateClientAuthentication = issuerConfig.getOptionalString("azure.require_certificate_client_authentication", false)
            Azure(
                    issuer = issuer,
                    jwksUri = URL(jwksUrl),
                    audience = audience,
                    alias = alias,
                    authorizedClients = authorizedClient,
                    requiredGroups = requiredGroups,
                    requiredRoles = requiredRoles,
                    requireCertificateClientAuthentication = requireCertificateClientAuthentication != null && "true".equals(requireCertificateClientAuthentication, true)
            )
        } else {
            Issuer(
                    issuer = issuer,
                    jwksUri = URL(jwksUrl),
                    audience = audience,
                    alias = alias
            )
        }
        issuers[alias] = resolvedIssuer
    }
    logger.info("${issuers.size} Issuers konfigueret.")
    return issuers.toMap()
}

@KtorExperimentalAPI
fun ApplicationConfig.clients(path: String = "nav.auth.clients") : Map<String, Client> {
    val clientsConfigList = configList(path)
    if (clientsConfigList.isNullOrEmpty()) return emptyMap()
    val clients = mutableMapOf<String, Client>()
    for (clientConfig in clientsConfigList) {
        val alias = clientConfig.getRequiredString("alias", false)
        logger.info("Client[$alias]")
        val clientId = clientConfig.getOptionalString("client_id", false)
        if (clientId == null) {
            logger.info("Client[$alias] ikke konfigurert.")
            continue
        } else {
            logger.info("Client[$alias] er konfigurert.")
        }
        val clientSecret = clientConfig.getOptionalString("client_secret", true)
        val privateKeyJwk = clientConfig.getOptionalString("private_key_jwk", true)
        if (clientSecret != null && privateKeyJwk != null) throw IllegalStateException("Både 'private_key_jwk' og 'client_secret' satt for Client[$alias]. Kun en av disse kan settes per client.")
        if (clientSecret == null && privateKeyJwk == null) throw IllegalStateException("Hverken 'private_key_jwk' eller 'client_secret' satt for Client[$alias]. En av disse må settes per client.")

        val discoveryJson = runBlocking { clientConfig.getOptionalString("discovery_endpoint", false)?.discover(listOf(TOKEN_ENDPOINT)) }
        val tokenEndpoint = URL(if (discoveryJson != null) discoveryJson[TOKEN_ENDPOINT] as String else clientConfig.getRequiredString(TOKEN_ENDPOINT, false))
        logger.info("Client[$alias].token_endpoint = '$tokenEndpoint'")

        val resolvedClient = if (clientSecret != null) {
            ClientSecretClient(clientId, tokenEndpoint, clientSecret)
        } else {
            val certificateHexThumbprint = clientConfig.getRequiredString("certificate_hex_thumbprint", false)
            PrivateKeyClient(clientId, tokenEndpoint, privateKeyJwk!!, certificateHexThumbprint)
        }
        clients[alias] = resolvedClient
    }
    logger.info("${clients.size} clients konfigurert.")
    return clients.toMap()
}

private fun String.discover(requiredAttributes : List<String>) : JSONObject? {
    val asText = URL(this).readText()
    val asJson = jsonParser.parse(asText) as JSONObject
    return if (asJson.containsKeys(requiredAttributes)) asJson else {
        logger.warn("Response fra Discovery Endpoint inneholdt ikke attributtene '[${requiredAttributes.joinToString()}]'. Response='$asText'")
        null
    }
}

private fun JSONObject.containsKeys(requiredAttributes: List<String>): Boolean {
    requiredAttributes.forEach {
        if (!containsKey(it)) return false
    }
    return true
}