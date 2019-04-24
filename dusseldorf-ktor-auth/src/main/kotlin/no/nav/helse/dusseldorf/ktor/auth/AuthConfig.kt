package no.nav.helse.dusseldorf.ktor.auth

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import java.net.URL

private const val AZURE_TYPE = "azure"

@KtorExperimentalAPI
fun ApplicationConfig.jwtIssuers(path: String = "nav.auth.issuers") : Map<String, Issuer> {
    val issuersConfigList = configList(path)
    if (issuersConfigList.isNullOrEmpty()) return emptyMap()
    val issuers = mutableMapOf<String, Issuer>()
    issuersConfigList.forEach { issuerConfig ->
        // Required
        val alias = issuerConfig.getRequiredString("alias", false)
        val issuer = issuerConfig.getRequiredString("issuer", false)
        val jwksUrl = URL(issuerConfig.getRequiredString("jwks_uri", false))
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
            Azure(issuer, jwksUrl, audience, authorizedClient, requiredGroups, requiredRoles, requireCertificateClientAuthentication = requireCertificateClientAuthentication != null && "true".equals(requireCertificateClientAuthentication, true))
        } else {
            Issuer(issuer, jwksUrl, audience)
        }
        issuers[alias] = resolvedIssuer
    }
    return issuers.toMap()
}

@KtorExperimentalAPI
fun ApplicationConfig.oauth2Clients(path: String = "nav.auth.clients") : Map<String, Client> {
    val clientsConfigList = configList(path)
    if (clientsConfigList.isNullOrEmpty()) return emptyMap()
    val clients = mutableMapOf<String, Client>()
    clientsConfigList.forEachIndexed { index, clientConfig ->
        val alias = clientConfig.getRequiredString("alias", false)
        val clientSecret = clientConfig.getOptionalString("client_secret", true)
        val privateKeyJwk = clientConfig.getOptionalString("private_key_jwk", true)
        if (clientSecret == null && privateKeyJwk == null) throw IllegalStateException("Enten '$path[$index].client_secret' eller '$path[$index].private_key_jwk' må settes.")
        if (clientSecret != null && privateKeyJwk != null) throw IllegalStateException("Både '$path[$index].client_secret' og '$path[$index].private_key_jwk' kan ikke settes for samme en og samme client.")
        val clientId = clientConfig.getRequiredString("client_id", false)
        val tokenEndpoint = URL(clientConfig.getRequiredString("token_endpoint", false))

        val resolvedClient = if (clientSecret != null) {
            ClientSecretClient(clientId, tokenEndpoint, clientSecret)
        } else {
            PrivateKeyClient(clientId, tokenEndpoint, privateKeyJwk!!)
        }
        clients[alias] = resolvedClient
    }
    return clients.toMap()
}