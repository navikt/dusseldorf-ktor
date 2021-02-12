package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import okhttp3.internal.closeQuietly
import java.net.ProxySelector

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

    suspend fun String.httpGet(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        httpRequest(HttpMethod.Get, block)
    suspend fun String.httpPost(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        httpRequest(HttpMethod.Post, block)
    suspend fun String.httpPut(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        httpRequest(HttpMethod.Put, block)
    suspend fun String.httpPatch(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        httpRequest(HttpMethod.Patch, block)
    suspend fun String.httpDelete(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        httpRequest(HttpMethod.Delete, block)
    suspend fun String.httpOptions(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        httpRequest(HttpMethod.Options, block)
    suspend fun String.httpHead(block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        httpRequest(HttpMethod.Head, block)

    suspend fun Result<HttpResponse>.readTextOrThrow() =
        getOrThrow().let { it.status to it.readText() }
    suspend fun Pair<HttpRequestData, Result<HttpResponse>>.readTextOrThrow() =
        second.readTextOrThrow()

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