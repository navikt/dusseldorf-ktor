package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.charsets.Charsets
import okhttp3.OkHttpClient
import okhttp3.internal.closeQuietly
import org.json.JSONObject
import java.net.Proxy
import java.net.ProxySelector
import java.nio.charset.Charset

object SimpleHttpClient {
    private fun newHttpClient(block: (builder: OkHttpClient.Builder) -> Unit) = HttpClient(OkHttp) {
        install(HttpTimeout)
        this.expectSuccess = false
        engine {
            config {
                block(this)
                retryOnConnectionFailure(true)
            }
        }
    }.also {
        Runtime.getRuntime().addShutdownHook(Thread { it.closeQuietly() })
    }

    private val httpClientProxySelector = newHttpClient {
        it.proxySelector(ProxySelector.getDefault())
    }

    private val httpClientNoProxy = newHttpClient {
        it.proxy(Proxy.NO_PROXY)
    }

    private fun Boolean.httpClient() = when (this) {
        true -> httpClientNoProxy
        false -> httpClientProxySelector
    }

    suspend fun Any.httpGet(noProxy: Boolean = false, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(noProxy, HttpMethod.Get, block)
    suspend fun Any.httpPost(noProxy: Boolean = false, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(noProxy, HttpMethod.Post, block)
    suspend fun Any.httpPut(noProxy: Boolean = false, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(noProxy, HttpMethod.Put, block)
    suspend fun Any.httpPatch(noProxy: Boolean = false, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(noProxy, HttpMethod.Patch, block)
    suspend fun Any.httpDelete(noProxy: Boolean = false, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(noProxy, HttpMethod.Delete, block)
    suspend fun Any.httpOptions(noProxy: Boolean = false, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(noProxy, HttpMethod.Options, block)
    suspend fun Any.httpHead(noProxy: Boolean = false, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(noProxy, HttpMethod.Head, block)

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
        noProxy: Boolean,
        httpMethod: HttpMethod,
        block: (httpRequestBuilder: HttpRequestBuilder) -> Unit
    ) : Pair<HttpRequestData, Result<HttpResponse>> {

        lateinit var httpRequestData : HttpRequestData

        val httpStatement = this.let { url -> noProxy.httpClient().request<HttpStatement> {
            this.url(url)
            this.method = httpMethod
            block(this)
            httpRequestData = build()
        }}

        return httpRequestData to httpStatement.runCatching { execute() }
    }
}
