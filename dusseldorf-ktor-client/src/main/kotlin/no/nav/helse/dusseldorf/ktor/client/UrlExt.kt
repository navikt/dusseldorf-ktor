package no.nav.helse.dusseldorf.ktor.client

import io.ktor.http.*
import io.ktor.http.URLBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.client.UrlExt")

fun Url.Companion.buildURL(
    baseUrl: URL,
    pathParts: List<String> = listOf(),
    queryParameters: Map<String, List<String>> = mapOf()
) : URL {
    val withBasePath= mutableListOf(baseUrl.path)
    withBasePath.addAll(pathParts)

    val parametersBuilder = ParametersBuilder()
    queryParameters.forEach { queryParameter ->
        queryParameter.value.forEach { it ->
            parametersBuilder.append(queryParameter.key, it)
        }
    }

    val urlBuilder = URLBuilder(parameters = parametersBuilder)
            .takeFrom(baseUrl.toString())
            .trimmedPath(withBasePath)

    val url = urlBuilder.build().toURI().toURL()
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
    return path(trimmedPathParts)
}