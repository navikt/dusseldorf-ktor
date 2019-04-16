package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwt.impl.NullClaim
import com.auth0.jwt.interfaces.Claim

interface EnforcementOutcome
data class Successful(val claimName: String, val claimValue: Any) : EnforcementOutcome
data class Failure(val claimName: String, val expected: Any, val actual: Any?) : EnforcementOutcome

interface ClaimRule {
    fun enforce(claims: Map<String, Claim>) : EnforcementOutcome
}
abstract class ClaimRuleResolvableClaimName(private val defaultClaimName : String?) : ClaimRule {
     open fun resolveClaimName(claims: Map<String, Claim>) : String = defaultClaimName!!
}

abstract class EnforceEqualsOrContains(
        defaultClaimName : String? = null,
        private val expected : String
) : ClaimRuleResolvableClaimName(defaultClaimName) {
    override fun enforce(claims: Map<String, Claim>): EnforcementOutcome {
        val resolvedClaimName = resolveClaimName(claims)
        val claimValue = claims[resolvedClaimName]
        if (claimValue == null || claimValue is NullClaim) return Failure(resolvedClaimName, expected, null)

        val stringValue = claimValue.asString()
        val listValue = claimValue.asList(String::class.java)

        return when {
            stringValue != null -> if (expected == stringValue) {
                Successful(resolvedClaimName, stringValue)
            } else Failure(resolvedClaimName, expected, stringValue)
            listValue != null -> if (listValue.contains(expected)) {
                Successful(resolvedClaimName, expected)
            } else Failure(resolvedClaimName, expected, listValue.joinToString())
            else -> Failure(resolvedClaimName, expected, null)
        }
    }
}

abstract class EnforceContainsAll(
        defaultClaimName : String? = null,
        private val all : Set<String>
) : ClaimRuleResolvableClaimName(defaultClaimName) {
    private val allError = "[${all.joinToString()}]"
    override fun enforce(claims: Map<String, Claim>): EnforcementOutcome {
        val resolvedClaimName = resolveClaimName(claims)
        val claimValue = claims[resolvedClaimName]?.asList(String::class.java)
        if (claimValue == null) return Failure(resolvedClaimName, allError, claimValue)

        return if (claimValue.containsAll(all)) {
            Successful(resolvedClaimName, claimValue.joinToString())
        } else Failure(resolvedClaimName, allError, "[${claimValue.joinToString()}]")
    }
}

abstract class EnforceContainsOneOf(
        defaultClaimName : String? = null,
        private val oneOf : Set<String>
) : ClaimRuleResolvableClaimName(defaultClaimName) {
    private val oneOfError = "En av [${oneOf.joinToString()}]"

    override fun enforce(claims: Map<String, Claim>): EnforcementOutcome {
        val resolvedClaimName = resolveClaimName(claims)
        val claimValue = claims[resolvedClaimName]?.asString()
        if (claimValue == null) return Failure(resolvedClaimName, oneOfError, claimValue)

        return if (oneOf.contains(claimValue)) {
            Successful(resolvedClaimName, claimValue)
        } else Failure(resolvedClaimName, oneOfError, claimValue)
    }
}

class ClaimEnforcementFailed(
        val outcomes: Set<Set<EnforcementOutcome>>) : RuntimeException("Håndhevelse av claims i token feilet. Ingen av settene med regler passerte sjekken. Resultat = '$outcomes'")

class ClaimEnforcer(
        private val oneOf: Set<Set<ClaimRule>>
) {
    fun enforce(claims: Map<String, Claim>) : Set<Successful> {
        val outcomes = mutableSetOf<Set<EnforcementOutcome>>()

        oneOf.forEach { ruleSet ->
            val success = mutableSetOf<Successful>()
            val failure = mutableSetOf<Failure>()

            ruleSet.forEach { rule ->
                val outcome = rule.enforce(claims)
                when (outcome) {
                    is Successful -> success.add(outcome)
                    else -> failure.add(outcome as Failure)
                }
            }
            if (failure.isEmpty()) return success
            else {
                val rulesOutcome = mutableSetOf<EnforcementOutcome>()
                rulesOutcome.addAll(success)
                rulesOutcome.addAll(failure)
                outcomes.add(rulesOutcome)
            }
        }
        throw ClaimEnforcementFailed(outcomes)
    }
}