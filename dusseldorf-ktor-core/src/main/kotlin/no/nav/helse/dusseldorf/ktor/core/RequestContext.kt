package no.nav.helse.dusseldorf.ktor.core

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

object RequestContext {

    data class Context(private val context: Map<String, Any>) {
        fun getRequired(key: String) = context.getValue(key)
        fun getOptional(key: String) = context[key]
        fun correlationId() = getRequired(HttpHeaders.XCorrelationId)
        fun authorizationHeader() = getRequired(HttpHeaders.Authorization)
    }

    private fun Map<String, Any?>.filterNotNullValues() = filterValues { it != null }.mapValues { it.value!! }

    fun CoroutineContext.requestContext() =
        get(CoroutineRequestContext.Key)?.context ?: throw IllegalStateException("Request Context ikke satt.")

    suspend fun <T>withRequestContext(
        call: ApplicationCall,
        block: suspend CoroutineScope.() -> T) = withContext(
        context = coroutineContext + CoroutineRequestContext(Context(call.initializeRequestContext().filterNotNullValues())),
        block = block
    )

    fun ApplicationCall.initializeRequestContext() = mapOf(
        HttpHeaders.XCorrelationId to (callId ?: request.header(HttpHeaders.XCorrelationId)),
        HttpHeaders.Authorization to (request.authorization() ?: request.header(HttpHeaders.Authorization))
    )

    suspend fun <T>withRequestContext(
        context: Map<String, Any?>,
        block: suspend CoroutineScope.() -> T) = withContext(
        context = coroutineContext + CoroutineRequestContext(Context(context.filterNotNullValues())),
        block = block
    )

    private class CoroutineRequestContext(
        val context: Context
    ) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<CoroutineRequestContext>
    }
}

