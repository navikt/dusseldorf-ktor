package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.charsets.Charsets
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.closeQuietly
import org.json.JSONObject
import java.net.ProxySelector
import java.nio.charset.Charset

object SimpleHttpClient {

    data class Config(
        val engine: HttpClientEngineFactory<HttpClientEngineConfig>) {
        internal companion object {
            internal val defaultConfig = Config(engine = OkHttp)
        }
    }

    private val okHttpClient = HttpClient(OkHttp) {
        install(HttpTimeout)
        install(DefaultRequest)
        expectSuccess = false
        engine {
            config {
                proxySelector(ProxySelector.getDefault())
                retryOnConnectionFailure(true)
            }
        }
    }.also {
        Runtime.getRuntime().addShutdownHook(Thread { it.closeQuietly() })
    }

    private val javaHttpClient = HttpClient(Java) {
        install(HttpTimeout)
        expectSuccess = false
        engine {
            config {
                proxy(ProxySelector.getDefault())
            }
        }
    }.also {
        Runtime.getRuntime().addShutdownHook(Thread { it.closeQuietly() })
    }

    private fun Config.httpClient() = when (engine) {
        OkHttp -> okHttpClient
        Java -> javaHttpClient
        else -> throw IllegalStateException("Ikke støttet http engine ${engine.javaClass.simpleName}")
    }

    suspend fun String.httpGet(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}): Pair<HttpRequestData, Result<HttpResponse>> =
        httpRequest(config, HttpMethod.Get, block)
    suspend fun String.httpPost(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}): Pair<HttpRequestData, Result<HttpResponse>> =
        httpRequest(config, HttpMethod.Post, block)
    suspend fun String.httpPut(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}): Pair<HttpRequestData, Result<HttpResponse>> =
        httpRequest(config, HttpMethod.Put, block)
    suspend fun String.httpPatch(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}): Pair<HttpRequestData, Result<HttpResponse>> =
        httpRequest(config, HttpMethod.Patch, block)
    suspend fun String.httpDelete(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}): Pair<HttpRequestData, Result<HttpResponse>> =
        httpRequest(config, HttpMethod.Delete, block)
    suspend fun String.httpOptions(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}): Pair<HttpRequestData, Result<HttpResponse>> =
        httpRequest(config, HttpMethod.Options, block)
    suspend fun String.httpHead(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}): Pair<HttpRequestData, Result<HttpResponse>> =
        httpRequest(config, HttpMethod.Head, block)

    suspend fun Result<HttpResponse>.readTextOrThrow() =
        getOrThrow().let { it.status to it.bodyAsText() }
    suspend fun Pair<HttpRequestData, Result<HttpResponse>>.readTextOrThrow() =
        second.readTextOrThrow()

    fun HttpRequestBuilder.stringBody(string: String, charset: Charset = Charsets.UTF_8, contentType: ContentType) {
        setBody(ByteArrayContent(bytes = string.toByteArray(charset), contentType = contentType))
    }
    fun HttpRequestBuilder.jsonBody(json: String) =
        stringBody(string = JSONObject(json).toString(), contentType = ContentType.Application.Json)

    private suspend fun String.httpRequest(
        config: Config,
        httpMethod: HttpMethod,
        block: (httpRequestBuilder: HttpRequestBuilder) -> Unit
    ) : Pair<HttpRequestData, Result<HttpResponse>> {

        lateinit var httpRequestData : HttpRequestData

        val httpStatement: HttpResponse = this.let { url: String -> config.httpClient().request {
            this.url(url)
            this.method = httpMethod
            block(this)
            httpRequestData = build()
        }}

        return httpRequestData to httpStatement.runCatching { this.body() }
    }
}
