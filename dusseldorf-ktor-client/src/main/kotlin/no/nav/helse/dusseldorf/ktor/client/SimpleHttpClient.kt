package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.charsets.Charsets
import okhttp3.internal.closeQuietly
import org.json.JSONObject
import java.net.ProxySelector
import java.nio.charset.Charset

object SimpleHttpClient {
    private val httpClient = HttpClient(OkHttp) {
        install(HttpTimeout)
        this.expectSuccess = false
        engine {
            config {
                proxySelector(ProxySelector.getDefault())
                retryOnConnectionFailure(true)
            }
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            httpClient.closeQuietly()
        })
    }

    suspend fun Any.httpGet(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(HttpMethod.Get, block)
    suspend fun Any.httpPost(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(HttpMethod.Post, block)
    suspend fun Any.httpPut(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(HttpMethod.Put, block)
    suspend fun Any.httpPatch(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(HttpMethod.Patch, block)
    suspend fun Any.httpDelete(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(HttpMethod.Delete, block)
    suspend fun Any.httpOptions(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(HttpMethod.Options, block)
    suspend fun Any.httpHead(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(HttpMethod.Head, block)

    suspend fun Result<HttpResponse>.readTextOrThrow() =
        getOrThrow().let { it.status to it.readText() }
    suspend fun Pair<HttpRequestData, Result<HttpResponse>>.readTextOrThrow() =
        second.readTextOrThrow()

    fun HttpRequestBuilder.stringBody(string: String, charset: Charset = Charsets.UTF_8, contentType: ContentType) {
        body = ByteArrayContent(bytes = string.toByteArray(charset), contentType = contentType)
    }
    fun HttpRequestBuilder.jsonBody(json: String) =
        stringBody(string = JSONObject(json).toString(), contentType = ContentType.Application.Json)

    private suspend fun String.httpRequest(
        httpMethod: HttpMethod,
        block: (httpRequestBuilder: HttpRequestBuilder) -> Unit
    ) : Pair<HttpRequestData, Result<HttpResponse>> {

        lateinit var httpRequestData : HttpRequestData

        val httpStatement = this.let { url -> httpClient.request<HttpStatement> {
            this.url(url)
            this.method = httpMethod
            block(this)
            httpRequestData = build()
        }}

        return httpRequestData to httpStatement.runCatching { execute() }
    }
}