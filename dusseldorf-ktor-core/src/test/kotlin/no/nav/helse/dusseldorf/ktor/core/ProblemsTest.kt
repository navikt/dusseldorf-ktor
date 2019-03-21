package no.nav.helse.dusseldorf.ktor.core

import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class ProblemsTest {

    @Test
    fun `test`() {
        val violations = setOf(
                Violation(
                        parameterName = "eier",
                        parameterType = ParameterType.QUERY,
                        reason = "Kan ikke være bokstaver",
                        invalidValue = "abc"
                ),
                Violation(
                        parameterName = "soker_id",
                        parameterType = ParameterType.PATH,
                        reason = "Er ikke satt",
                        invalidValue = true

                )
        )

        val problemDetails = ValidationProblemDetails(violations)
        assertEquals(problemDetails.status, 400)
        assertEquals(problemDetails.title, "invalid-request-parameters")
        assertEquals(problemDetails.type, URI.create("/problem-details/invalid-request-parameters"))
        assertEquals(problemDetails.instance, URI.create("about:blank"))

        val invalidParameters : List<Map<String, Any?>> = problemDetails.asMap()["invalid_parameters"] as List<Map<String, Any?>>

        assertEquals(2, invalidParameters.size)

        assertEquals(invalidParameters[0]["type"], "query")
        assertEquals(invalidParameters[0]["name"], "eier")
        assertEquals(invalidParameters[0]["reason"], "Kan ikke være bokstaver")
        assertEquals(invalidParameters[0]["invalid_value"], "abc")

        assertEquals(invalidParameters[1]["type"], "path")
        assertEquals(invalidParameters[1]["name"], "soker_id")
        assertEquals(invalidParameters[1]["reason"], "Er ikke satt")
        assertEquals(invalidParameters[1]["invalid_value"], true)
    }
}