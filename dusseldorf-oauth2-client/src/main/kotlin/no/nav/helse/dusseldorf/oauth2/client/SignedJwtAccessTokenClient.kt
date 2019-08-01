package no.nav.helse.dusseldorf.oauth2.client

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import org.slf4j.LoggerFactory
import java.net.URI
import java.security.KeyFactory
import java.time.*
import java.util.*
import java.security.cert.CertificateFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec

private val logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.oauth2.client.ClientAuthenticationPrivateKeyJwt")

class SignedJwtAccessTokenClient(
        private val clientId: String,
        privateKeyProvider: PrivateKeyProvider,
        keyIdProvider: KeyIdProvider,
        private val tokenEndpoint: URI
) : AccessTokenClient, NimbusAccessTokenClient() {
    private val jwsSigner: JWSSigner
    private val algorithm : JWSAlgorithm = JWSAlgorithm.RS256
    private val jwsHeader : JWSHeader

    init {
        jwsSigner = RSASSASigner(privateKeyProvider.getPrivateKey())
        jwsHeader = JWSHeader.Builder(algorithm)
                .keyID(keyIdProvider.getKeyId())
                .type(JOSEObjectType.JWT)
                .build()
    }


    override fun getAccessToken(scopes: Set<String>) : AccessTokenResponse {
        return getAccessToken(getClientCredentialsTokenRequest(scopes))
    }

    override fun getAccessToken(scopes: Set<String>, onBehalfOf: String) : AccessTokenResponse {
        return getAccessToken(getOnBehalfOfTokenRequest(onBehalfOf, scopes))
    }

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow#second-case-access-token-request-with-a-certificate
    private fun getClientCredentialsTokenRequest(
            scopes: Set<String>
    ) : TokenRequest = TokenRequest(
            tokenEndpoint,
            PrivateKeyJWT(getSignedJwt()),
            ClientCredentialsGrant(),
            getScope(scopes)
    )

    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow#second-case-access-token-request-with-a-certificate
    private fun getOnBehalfOfTokenRequest(
            onBehalfOf: String,
            scopes: Set<String>
    ) : TokenRequest = TokenRequest(
            tokenEndpoint,
            PrivateKeyJWT(getSignedJwt()),
            JWTBearerGrant(SignedJWT.parse(onBehalfOf)),
            getScope(scopes),
            null,
            onBehalfOfParameters
    )

    private fun getSignedJwt() : SignedJWT {
        val jwtClaimSet = JWTClaimsSet.Builder()
                .audience(tokenEndpoint.toString())
                .subject(clientId)
                .issuer(clientId)
                .jwtID(UUID.randomUUID().toString())
                .notBeforeTime(getNotBeforeTime())
                .expirationTime(getExpirationTime())
                .build()

        val signedJwt = SignedJWT(jwsHeader, jwtClaimSet)
        signedJwt.sign(jwsSigner)
        return signedJwt
    }

    private fun getNotBeforeTime() : Date {
        val now = LocalDateTime.now(Clock.systemUTC())
        return Date.from(now.toInstant(ZoneOffset.UTC))
    }

    private fun getExpirationTime() : Date {
        val exp = LocalDateTime.now(Clock.systemUTC()).plusSeconds(10)
        return Date.from(exp.toInstant(ZoneOffset.UTC))
    }
}

interface KeyIdProvider{
    fun getKeyId() : String
}
class DirectKeyId(private val keyId: String) : KeyIdProvider {
    override fun getKeyId(): String = keyId
}
class FromCertificateHexThumbprint(private val hexThumbprint: String) : KeyIdProvider {
    override fun getKeyId(): String  = Base64.getUrlEncoder().encodeToString(hexStringToByteArray(hexThumbprint))
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
    fun getPrivateKey() : RSAPrivateKey
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
        val trimmedPem = pem
                .replace("\n", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
        val encoded = Base64.getDecoder().decode(trimmedPem)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }
}

fun main() {
    val pleiepengerDokument = SignedJwtAccessTokenClient(
            clientId = "97f0b1bc-6aa9-4d44-a3c7-60b4318fbec4",
            privateKeyProvider = FromJwk("""
{"kty":"RSA","n":"zE2f0w4qXfIKiKkeG10zRyWqpabPVlKswJ0x-jvpO2y9XDimybktksvO4RkScLrJfARo4qiZ0p6gRATZ_KeSuvyYkyLjho8xKzNz4HYwB1TO4E6xQhdXALOXQHtMm12qKpWXQpnxxGA2kGNFlBam8ApLJuNGyiZefcqFE04R0T3jVbdYB0i3eHxJZFgGn-JhfX60eDJNr4ooUDbrpxNxZFuJXp201AuTMAlwwaIdWs60wzreRzVpxDbj178P5LI5zvXVIDaVTJeVpo_5j6CJqBA7kamRlS56wt2Xt_QdXY1qgyD5fMZkszsR_vKifQPsWuM4tB7U5atE3fQTjZpGyQ","e":"AQAB","d":"uzBYQtmUyfTt398MHchcJlEFtLdM-2vOqsCcvQjNnsv4CBpD_0nyzrB8QdRmB_GskDu-GgaewdLjRs8nJ44mK7sq1I0_6NckFtAS2NORF_aFghG36bVW2uaxq7wNKcE9G71qFPTWBrac59h9NL04gXD9AIl3H9rd8uVu3oxGWO52CS0tLWNf1Zuq-lh3WdFGDM0iKgs-_tSsPB3wdl7jyEh1OnUhhPQu8xLikBAcPz58udCUgG5LI2FG3gVhxO4Zw1dCXTk_NtPuxyGUUs4it6lfl50xwvBkXUpkacrMYJN66bDScAoBcVzbnq5xxMzkQ3tB1trnfGnv223sN2atMQ","p":"95NcrBnZAvTV0G3ZV31KyjLLDo_fqTkdpFUThbj833xAn7mA78t2GT1BCoSeZ9YCzqm3RY4QRHmtSy1c-pdXszte-lYrwz455RC5E8-ZOWgmh2HAI83TJH_mQbe_lDix6jWKKPIUlDgm3BQKuxI0oTh1PQsuvdtRkdk6hp1fnbU","q":"00FQrl0birZgoehl_gJ9q6czzt-BJ7i5a-K1az2mHHspznq6p08rNp3WM4jpYJ8gv2iff6roDdTa58zXsTcICS-Y3BMfIie6Vnvfz2VWTVsQzlalIyLGT6Iq002vnxtm59dtiVp0AcJOGSADjLsfraH_uk-oBokmAVsD9tNE0UU","dp":"wCVu12N4OgMrrwiSloR3d1bUogin3f6mMtSUwkRAfnNA9LZUi8etcQlJYZBsIMrIgvzVcPZBSePly2L5tsOfHUwG-uPGM1IoPNAt1GJa2WMsBAapySAFr15Upsyls7QM4WmMQRshPagXANfNPEN09WuMEkSLkm6VcEzYT4H0o8E","dq":"SLEl6QnCy2VcNPo59kC3K5S6QAoqcBehPh1hncg124EJ7rHOfnnAfeer6EnJPGUlKJgAojaV6QSAWtjis2kjS73kFH5D7UXMWawAZGYTX0ThsI6h_kgeAxLjN5h4wP_11fsMBwJn_p70rrKUZh2Rlfazf1GfmLgiqr69y3YBLR0","qi":"X9zPxR2rmEJm7obPJd7E-YVhtJ4WGSsOTIPxX7Q8EHwjjik4ST0YGl7Zgv_IT_ltx7Gt35fKdaxnagt07Cn72D7-IifZqeO-AFXV-kfhc8SvRicOrzC9FpiXyUAYVoswSXULXGel-uk0Z59pJFrdxixv_jZgUqkQH0Kz8k6RHsE"}
            """.trimIndent()),
            keyIdProvider = FromCertificateHexThumbprint("204AB2B9C5B4B6C18D980E3417E4E0BE9C82D2EF"),
            tokenEndpoint = URI("https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/oauth2/v2.0/token")
    )

    val pleiepengerDokumentToken = pleiepengerDokument.getAccessToken(setOf("b32ae17c-0276-4006-9507-4ef49e0e5e20/.default")).accessToken // Scope til joark

    println(pleiepengerDokumentToken)

    val pleiepengerJoark = SignedJwtAccessTokenClient(
            clientId = "b32ae17c-0276-4006-9507-4ef49e0e5e20",
            privateKeyProvider = FromJwk("""
{"kty":"RSA","n":"sce8ZzQG2T8O3RGSyIOazqnSKlrxS4ytN_cZWxH96droUdQWfowDWG3uMJfWGTWUpe8xJBOjDRyciHwFSgZrCspdsIgiYMgr7JHvksb2QOv8OX4cuAxtHB8E32zH_9zPRX6qy1lY6LkXN_6ai7lG6gqJHnLL1moboJG5vbktc5tF6TpF2KCP1dKmhoieiK5-FUGaft2q2pRgBG5H5h9bTqspmR7SAskLZeX5QKgJLCavH5zCIYN8YMyo4MzZZOt5Z129HnuhIrGH1i7BvTlAnHEdRGLLf2KpUcXz2ZxiO8a6w_V0GVNAw94E3mCJmTe_syAu6lq_QeOqorjFQmUTMQ","e":"AQAB","d":"p6dg-UKLItzgu4ITFp1lpHdqBx90Znbn89tHQicZeSxD4sdL31NS5FXnRlQqR-PLpyCHCyqpnXfZ9qxeoIyk7awh5pS7NuIO6i5F6_lNybPCTMWtd7PgQaDPC5Sel6A4p6TolJRR0EacquWkARhc0-HTpa9Eo5O1Mhu7OjwjrNyyx6O6gDpSwZ-kizHGIPJRZstTbTVaYgxrao7Hj5D-VllHp94CnEmKrDWZ18X7DH-NfXCD5NkDAK44lPlSBoQytebLd-k_MEsYQR5ZpBAN9KvR_IH4VxjPc0qsdulmji3G5mbckBmIAXsbxJOZdmQUorA-v6-g7rWLaVyFypw-MQ","p":"3dGts_30IlWS3sj83qKPdZQlk576DwRjnAxOTZxj-JjmcU2rJbriVzQcd8a9apvWppV80aDz0n7dToNhB66kYnaAhXloHNuQb42w7y-DF9QnP2fRBx5dhEhQyp7e2Vck06FS48yP5PT_ICSddEouuKITv3-sCmV-VEY2pcOIAN8","q":"zSzQC14oZcOB4S2KFleUnDU9QQtck7y2jNVfJQZpbgutf8ZZojKcKp5xOGMJPiyoqqiFCPOgLtq4_IpyTTbfjEU1SjT4g4agfOLh83tUfwS5HPg9Lm34zqG64eD_Y3Mz-6E6SSizjUTojDfNIcKPoloLt4GvrnSu7QX4pB0NHe8","dp":"j0ZvN1JyTXcOnb4er0M6_IxLVTWbePWxzUqZjUHVxO4l8stjH8S9i-wJ80e4c1Lja8gkzlZzBuSQ17S-iLLCVSML0qrfl7wuaA-SWHxM5ahQDYFmH336k4YOQUIq2SGTlOqf2qn3y8B_xehAl6uO0A1ISioSGskBtzd-9sT8ZOs","dq":"yLEh20zBxTDk1Ub_QHVwlw94mCwt5RD9m2qHA6du70aa8lYpjP9mREXxR4-QFfIHFc97OdNchxhJRDWEMsUPC50S0EBaKgVA7IfdLpYfg5MyIwUqi0Tkz_Hg5Ft6VGP2oBcH9VHLt86EjctzaqduagccWOaQ2tul05E4sgb8sp8","qi":"RnP_SL4enH6LixupknzFMLKqyWX2vk1xy5EE4SBKLX8CCyXtIytbq5XEmyCJScdmal9zXEKdQSaKmF7PfBYrM8KgNrmSacKuyezgAu3hoAhGEu3rwH7YKy3uifLBY03jfamdKkStZhmdDiTgDkR3LH8LYp4jHDhR6DOCHjRTotk"}
            """.trimIndent()),
            keyIdProvider = FromCertificateHexThumbprint("56583B21E76F5B1F68947914576A7805F03CDC14"),
            tokenEndpoint = URI("https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/oauth2/v2.0/token")
    )

    val pleiepengerJoarkToken = pleiepengerJoark.getAccessToken(
            scopes = setOf("8caa7c32-1523-43a7-af95-63123938f45c/.default"),
            onBehalfOf = pleiepengerDokumentToken
    )

    println(pleiepengerJoarkToken.accessToken)



}