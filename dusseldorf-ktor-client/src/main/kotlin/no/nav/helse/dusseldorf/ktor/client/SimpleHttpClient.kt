package no.nav.helse.dusseldorf.ktor.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
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
    data class Config(
        val engine: HttpClientEngineFactory<HttpClientEngineConfig>) {
        internal companion object {
            internal val defaultConfig = Config(engine = OkHttp)
        }
    }

    private val okHttpClient = HttpClient(OkHttp) {
        install(HttpTimeout)
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

    suspend fun Any.httpGet(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(config, HttpMethod.Get, block)
    suspend fun Any.httpPost(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(config, HttpMethod.Post, block)
    suspend fun Any.httpPut(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(config, HttpMethod.Put, block)
    suspend fun Any.httpPatch(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(config, HttpMethod.Patch, block)
    suspend fun Any.httpDelete(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(config, HttpMethod.Delete, block)
    suspend fun Any.httpOptions(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(config, HttpMethod.Options, block)
    suspend fun Any.httpHead(config: Config = Config.defaultConfig, block: (httpRequestBuilder: HttpRequestBuilder) -> Unit = {}) =
        toString().httpRequest(config, HttpMethod.Head, block)

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
        config: Config,
        httpMethod: HttpMethod,
        block: (httpRequestBuilder: HttpRequestBuilder) -> Unit
    ) : Pair<HttpRequestData, Result<HttpResponse>> {

        lateinit var httpRequestData : HttpRequestData

        val httpStatement = this.let { url -> config.httpClient().request<HttpStatement> {
            this.url(url)
            this.method = httpMethod
            block(this)
            httpRequestData = build()
        }}

        return httpRequestData to httpStatement.runCatching { execute() }
    }
}
