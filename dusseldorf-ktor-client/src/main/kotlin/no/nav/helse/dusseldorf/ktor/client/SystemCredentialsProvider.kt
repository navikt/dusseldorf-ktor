package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDateTime
import java.util.*

interface SystemCredentialsProvider {
    suspend fun getAuthorizationHeader() : String
}

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.client.Oauth2ClientCredentialsProvider")
class Oauth2ClientCredentialsProvider(
        tokenUrl : URL,
        clientId : String,
        clientSecret: String,
        scopes : List<String>,
        private val monitoredHttpClient: MonitoredHttpClient
) : SystemCredentialsProvider {
    private val httpRequestBuilder : HttpRequestBuilder
    private val completeUrl : URL

    @Volatile private var cachedToken: String? = null
    @Volatile private var expiry: LocalDateTime? = null

    init {
        val queryParameters : MutableMap<String, List<String>> = mutableMapOf(Pair("grant_type",listOf("client_credentials")))
        if (!scopes.isEmpty()) {
            queryParameters["scope"] = getScopesAsSpaceDelimitedList(scopes)
        }
        completeUrl = Url.buildURL(baseUrl = tokenUrl, queryParameters = queryParameters)

        logger.info("Oauth2 Access Tokens hentes token_url='$completeUrl' med client_id='$clientId'")

        httpRequestBuilder = HttpRequestBuilder()
        httpRequestBuilder.url(completeUrl)
        httpRequestBuilder.header(HttpHeaders.Authorization, getAuthorizationHeader(clientId, clientSecret))
        httpRequestBuilder.method = HttpMethod.Get
    }


    override suspend fun getAuthorizationHeader(): String {
        return "Bearer ${getToken()}"
    }

    private suspend fun getToken() : String {
        if (hasCachedToken() && isCachedTokenValid()) {
            logger.trace("Bruker cachet access token.")
            return cachedToken!!
        }

        clearCachedData()

        logger.info("Henter nytt access token.")
        val response = requestNewAccessToken()

        setCachedData(
                accessToken = response["access_token"] as String,
                expiresIn = response["expires_in"] as Long
        )

        return cachedToken!!
    }

    private suspend fun requestNewAccessToken() : Map<String, Any?> {
        return monitoredHttpClient.requestAndReceive(HttpRequestBuilder().takeFrom(httpRequestBuilder))
    }

    private fun getAuthorizationHeader(clientId: String, clientSecret: String) : String {
        val auth = "$clientId:$clientSecret"
        return "Basic ${Base64.getEncoder().encodeToString(auth.toByteArray())}"
    }

    private fun getScopesAsSpaceDelimitedList(scopes : List<String>) : List<String> {
        return listOf(scopes.joinToString(separator = " "))
    }


    private fun setCachedData(accessToken: String, expiresIn: Long) {
        cachedToken = accessToken
        expiry = LocalDateTime.now()
                .plusSeconds(expiresIn)
                .minusSeconds(10L)
    }

    private fun clearCachedData() {
        cachedToken = null
        expiry = null
    }

    private fun hasCachedToken() : Boolean {
        return cachedToken != null && expiry != null
    }

    private fun isCachedTokenValid() : Boolean {
        return expiry!!.isAfter(LocalDateTime.now())
    }

}