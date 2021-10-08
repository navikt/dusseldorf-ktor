package no.nav.helse.dusseldorf.oauth2.client

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import no.nav.helse.dusseldorf.oauth2.client.GrantType.Companion.grantType
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.*
import java.util.*
import java.security.cert.CertificateFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey

class SignedJwtAccessTokenClient(
    private val clientId: String,
    privateKeyProvider: PrivateKeyProvider,
    keyIdProvider: KeyIdProvider,
    private val tokenEndpoint: URI,
    private val onBehalfOfGrantType: GrantType = tokenEndpoint.grantType()
) : AccessTokenClient, NimbusAccessTokenClient() {
    private val jwsSigner: JWSSigner
    private val algorithm: JWSAlgorithm = JWSAlgorithm.RS256
    private val jwsHeader: JWSHeader

    private companion object {
        private val logger = LoggerFactory.getLogger(SignedJwtAccessTokenClient::class.java)
    }

    init {
        jwsSigner = RSASSASigner(privateKeyProvider.getPrivateKey())
        jwsHeader = JWSHeader.Builder(algorithm)
            .keyID(keyIdProvider.getKeyId())
            .type(JOSEObjectType.JWT)
            .build()
        logger.info("OnBehalfOfGrantType=$onBehalfOfGrantType")
    }


    override fun getAccessToken(scopes: Set<String>): AccessTokenResponse {
        return getAccessToken(getClientCredentialsTokenRequest(scopes))
    }

    override fun getAccessToken(scopes: Set<String>, onBehalfOf: String): AccessTokenResponse {
        return when (onBehalfOfGrantType) {
            GrantType.JwtBearer -> getAccessToken(getOnBehalfOfJwtBearerTokenRequest(onBehalfOf, scopes))
            GrantType.TokenExchange -> getAccessToken(getOnBehalfOfTokenExchangeTokenRequest(onBehalfOf, scopes))
        }
    }

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow#second-case-access-token-request-with-a-certificate
    private fun getClientCredentialsTokenRequest(
        scopes: Set<String>
    ): TokenRequest = TokenRequest(
        tokenEndpoint,
        PrivateKeyJWT(getSignedJwt()),
        ClientCredentialsGrant(),
        getScope(scopes)
    )

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow#second-case-access-token-request-with-a-certificate
    private fun getOnBehalfOfJwtBearerTokenRequest(
        onBehalfOf: String,
        scopes: Set<String>
    ) = TokenRequest(
        tokenEndpoint,
        PrivateKeyJWT(getSignedJwt()),
        JWTBearerGrant(SignedJWT.parse(onBehalfOf)),
        getScope(scopes),
        null,
        onBehalfOfParameters
    )

    // https://doc.nais.io/security/auth/tokenx/#exchanging-a-token
    private fun getOnBehalfOfTokenExchangeTokenRequest(
        onBehalfOf: String,
        scopes: Set<String>
    ) = TokenExchange(
        tokenEndpoint = tokenEndpoint,
        privateKeyJWT = PrivateKeyJWT(getSignedJwt()),
        scope = checkNotNull(getScope(scopes)) { "Token Exchange kan ikke gjøres uten scope" },
        onBehalfOf = SignedJWT.parse(onBehalfOf)
    ).toHTTPRequest()

    private fun getSignedJwt(): SignedJWT {
        val jwtClaimSet = JWTClaimsSet.Builder()
            .audience(tokenEndpoint.toString())
            .subject(clientId)
            .issuer(clientId)
            .jwtID(UUID.randomUUID().toString())
            .notBeforeTime(getNotBeforeTime())
            .expirationTime(getExpirationTime())
            .issueTime(getIssuedAtTime())
            .build()

        val signedJwt = SignedJWT(jwsHeader, jwtClaimSet)
        signedJwt.sign(jwsSigner)
        return signedJwt
    }

    private fun getNotBeforeTime(): Date {
        val now = LocalDateTime.now(Clock.systemUTC())
        return Date.from(now.toInstant(ZoneOffset.UTC))
    }

    private fun getIssuedAtTime(): Date {
        val now: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
        return Date.from(now.toInstant(ZoneOffset.UTC))
    }

    private fun getExpirationTime(): Date {
        val exp = LocalDateTime.now(Clock.systemUTC()).plusSeconds(10)
        return Date.from(exp.toInstant(ZoneOffset.UTC))
    }
}

interface KeyIdProvider {
    fun getKeyId(): String
}

class DirectKeyId(private val keyId: String) : KeyIdProvider {
    override fun getKeyId(): String = keyId
}

class FromCertificateHexThumbprint(private val hexThumbprint: String) : KeyIdProvider {
    override fun getKeyId(): String = Base64.getUrlEncoder().encodeToString(hexStringToByteArray(hexThumbprint))
    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}

class FromCertificatePem(private val pem: String) : KeyIdProvider {
    override fun getKeyId(): String {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = pem.byteInputStream().use {
            certificateFactory.generateCertificate(it)
        }
        val messageDigest = MessageDigest.getInstance("SHA-1")
        messageDigest.update(certificate.encoded)
        return Base64.getUrlEncoder().encodeToString(messageDigest.digest())
    }
}

interface PrivateKeyProvider {
    fun getPrivateKey(): RSAPrivateKey
}

class DirectPrivateKey(private val privateKey: RSAPrivateKey) : PrivateKeyProvider {
    override fun getPrivateKey(): RSAPrivateKey = privateKey
}

class FromJwk(private val jwk: String) : PrivateKeyProvider {
    override fun getPrivateKey(): RSAPrivateKey {
        return RSAKey.parse(jwk).toRSAPrivateKey()
    }
}

class FromPrivateKeyPem(private val pem: String) : PrivateKeyProvider {
    override fun getPrivateKey(): RSAPrivateKey {
        val jwk = JWK.parseFromPEMEncodedObjects(pem)
        return RSAKey.parse(jwk.toJSONObject()).toRSAPrivateKey()
    }
}
