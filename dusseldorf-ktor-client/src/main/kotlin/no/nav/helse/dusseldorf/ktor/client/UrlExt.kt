package no.nav.helse.dusseldorf.ktor.client

import io.ktor.http.*
import io.ktor.http.URLBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.client.UrlExt")

fun Url.Companion.buildURL(
    baseUrl: URI,
    pathParts: List<String> = listOf(),
    queryParameters: Map<String, List<String>> = mapOf()
) : URI {
    val withBasePath= mutableListOf(baseUrl.path)
    withBasePath.addAll(pathParts)

    val parameters = ParametersBuilder().apply {
        queryParameters.forEach { queryParameter ->
            queryParameter.value.forEach {
                append(queryParameter.key, it)
            }
        }
    }.build()

    val urlBuilder = URLBuilder(parameters = parameters)
            .takeFrom(baseUrl.toString())
            .trimmedPath(withBasePath)

    val url = urlBuilder.build().toURI()
    logger.trace("Built URL '$url'")
    return url
}

private fun URLBuilder.trimmedPath(pathParts : List<String>): URLBuilder  {
    val trimmedPathParts = mutableListOf<String>()
    pathParts.forEach { part ->
        if (part.isNotBlank()) {
            trimmedPathParts.add(part.trimStart('/').trimEnd('/'))
        }
    }
    path(*trimmedPathParts.toTypedArray())
    return this
}
