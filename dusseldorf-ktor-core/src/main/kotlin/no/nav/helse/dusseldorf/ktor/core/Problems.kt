package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationRequest
import io.ktor.response.respond
import java.net.URI

/*
    https://tools.ietf.org/html/rfc7807#section-3


   A problem details object can have the following members:

   o  "type" (string) - A URI reference [RFC3986] that identifies the
      problem type.  This specification encourages that, when
      dereferenced, it provide human-readable documentation for the
      problem type (e.g., using HTML [W3C.REC-html5-20141028]).  When
      this member is not present, its value is assumed to be
      "about:blank".

   o  "title" (string) - A short, human-readable summary of the problem
      type.  It SHOULD NOT change from occurrence to occurrence of the
      problem, except for purposes of localization (e.g., using
      proactive content negotiation; see [RFC7231], Section 3.4).

   o  "status" (number) - The HTTP status code ([RFC7231], Section 6)
      generated by the origin server for this occurrence of the problem.

   o  "detail" (string) - A human-readable explanation specific to this
      occurrence of the problem.

   o  "instance" (string) - A URI reference that identifies the specific
      occurrence of the problem.  It may or may not yield further
      information if dereferenced.
 */


interface Problem {
    fun getProblemDetails() : ProblemDetails
}

class Throwblem : Throwable, Problem {
    private val occurredProblemDetails: ProblemDetails

    constructor(problemDetails: ProblemDetails) : super(problemDetails.asMap().toString()) {
        this.occurredProblemDetails = problemDetails
    }

    constructor(problemDetails: ProblemDetails, throwable: Throwable) : super(problemDetails.asMap().toString(), throwable) {
        this.occurredProblemDetails = problemDetails
    }

    override fun getProblemDetails(): ProblemDetails = occurredProblemDetails
}

interface ProblemDetails {
    val title : String
    val type : URI
    val status : Int
    val detail : String
    val instance : URI
    fun asMap() : Map<String, Any>
}

suspend fun ApplicationCall.respondProblemDetails(problemDetails: ProblemDetails) {
    respond(
            status = HttpStatusCode.fromValue(problemDetails.status),
            message = problemDetails.asMap()
    )
}

open class DefaultProblemDetails(
        override val title : String,
        override val type : URI = URI("/problem-details/$title"),
        override val status : Int,
        override val detail : String,
        override val instance : URI = URI("about:blank")
) : ProblemDetails {
    override fun asMap() : Map<String, Any> {
        return mapOf(
                Pair("type", type.toString()),
                Pair("title", title),
                Pair("status", status),
                Pair("detail", detail),
                Pair("instance", instance.toString())
        )
    }
}

enum class ParameterType {
    QUERY,
    PATH,
    HEADER,
    ENTITY,
    FORM
}

data class Violation(val parameterName : String, val parameterType: ParameterType, val reason: String, val invalidValue : Any? = null)

data class ValidationProblemDetails(
        val violations : Set<Violation>

) : DefaultProblemDetails(
        title = "invalid-request-parameters",
        status = 400,
        detail = "Requesten inneholder ugyldige paramtere."
) {
    override fun asMap() : Map<String, Any> {
        val invalidParametersList : MutableList<Map<String, Any?>> = mutableListOf()
        violations.forEach{ it ->
            invalidParametersList.add(mapOf(
                    Pair("type", it.parameterType.name.toLowerCase()),
                    Pair("name", it.parameterName),
                    Pair("reason", it.reason),
                    Pair("invalid_value", it.invalidValue)
            ))
        }
        return super.asMap().toMutableMap().apply {
            put("invalid_parameters", invalidParametersList)
        }.toMap()
    }
}

