package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.Claim
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ClaimEnforcerTest {
    private val requiredAudience = "my-own-client"
    private val authorizedClients = setOf("clientA", "clientB", "clientC")
    private val requiredGroups = setOf("groupA","groupB","groupC")
    private val requiredRoles = setOf("roleA","roleB","roleC")

    private val enforcer = ClaimEnforcer(oneOf = setOf(
            setOf(
                    StandardClaimRules.Companion.EnforceAudienceEquals(requiredAudience),
                    AzureClaimRules.Companion.EnforceAuthorizedClient(authorizedClients),
                    AzureClaimRules.Companion.EnforceCertificateClientAuthentication(),
                    AzureClaimRules.Companion.EnforceInAllGroups(requiredGroups),
                    AzureClaimRules.Companion.EnforceHasAllRoles(requiredRoles)
            )
    ))

    @Test(expected = IllegalStateException::class)
    fun `Azure Token uten Versjon claim feiler`() {
        val claims = getClaims("{}")
        getOutcomes(enforcer, claims)
    }

    @Test(expected = IllegalStateException::class)
    fun `Azure token med ugyldig versjon feiler`() {
        val claims = getClaims("""{
            "ver" : "3.0"
        }""".trimIndent())
        getOutcomes(enforcer, claims)
    }

    @Test
    fun `Azure token 1_0 uten påkrevde claims feiler`() {
        val claims = getClaims("""{
            "ver" : "1.0"
        }""".trimIndent())
        val outcomes = getOutcomes(enforcer, claims).first()
        val failure = outcomes.filter { it is Failure }
        val successful = outcomes.filter { it is Successful }

        assertEquals(5, failure.size)
        assertEquals(0, successful.size)
    }

    @Test
    fun `Azure token 1_0 med riktige claims, men ikke medlem av riktige grupper`() {
        val claims = getClaims("""{
            "ver" : "1.0",
            "aud": "$requiredAudience",
            "appid": "clientA",
            "appidacr": "2",
            "roles": ["roleA","roleB","roleC"],
            "groups": ["feilgruppeA", "feilgruppeB"]
        }""".trimIndent())
        val outcomes = getOutcomes(enforcer, claims).first()
        val failure = outcomes.filter { it is Failure }
        val successful = outcomes.filter { it is Successful }

        assertEquals(1, failure.size)
        assertEquals(4, successful.size)

        assertEquals("groups", (failure.first() as Failure).claimName)
        assertEquals("[feilgruppeA, feilgruppeB]", (failure.first() as Failure).actual)
        assertEquals("[groupA, groupB, groupC]", (failure.first() as Failure).expected)
    }

    @Test
    fun `Azure token 1_0 med riktige claims`() {
        val claims = getClaims("""{
            "ver" : "1.0",
            "aud": "$requiredAudience",
            "appid": "clientA",
            "appidacr": "2",
            "roles": ["roleA","roleB","roleC"],
            "groups": ["groupA", "groupB", "groupC"]
        }""".trimIndent())
        val outcomes = getOutcomes(enforcer, claims).first()
        val failure = outcomes.filter { it is Failure }
        val successful = outcomes.filter { it is Successful }

        assertEquals(0, failure.size)
        assertEquals(5, successful.size)
    }

    @Test
    fun `Azure token 2_0 med riktige claims 1_0 claims`() {
        val claims = getClaims("""{
            "ver" : "2.0",
            "aud": "$requiredAudience",
            "appid": "clientA",
            "appidacr": "2",
            "roles": ["roleA","roleB","roleC"],
            "groups": ["groupA", "groupB", "groupC"]
        }""".trimIndent())
        val outcomes = getOutcomes(enforcer, claims).first()
        val failure = outcomes.filter { it is Failure }
        val successful = outcomes.filter { it is Successful }

        assertEquals(2, failure.size)
        assertEquals(3, successful.size)
    }

    @Test
    fun `Azure token 2_0 med riktige claims`() {
        val claims = getClaims("""{
            "ver" : "2.0",
            "aud": "$requiredAudience",
            "azp": "clientA",
            "azpacr": "2",
            "roles": ["roleA","roleB","roleC"],
            "groups": ["groupA", "groupB", "groupC"]
        }""".trimIndent())
        val outcomes = getOutcomes(enforcer, claims).first()
        val failure = outcomes.filter { it is Failure }
        val successful = outcomes.filter { it is Successful }

        assertEquals(0, failure.size)
        assertEquals(5, successful.size)
    }


    @Test
    fun `Azure token med scope håndteres riktig`() {
        val medAlle = getClaims("""{"scp":"en to tre"}""".trimIndent())
        val manglerEn = getClaims("""{"scp":"en to"}""".trimIndent())
        val uten = getClaims("{}")
        val tom = getClaims("""{"scp":""}""".trimIndent())

        val scopeEnforcer = ClaimEnforcer(oneOf = setOf(setOf(AzureClaimRules.Companion.EnforceHasAllScopes(setOf("en", "to", "tre")))))

        assertOutcome(getOutcomes(scopeEnforcer, medAlle).first(), 1, 0)
        assertOutcome(getOutcomes(scopeEnforcer, manglerEn).first(), 0, 1)
        assertOutcome(getOutcomes(scopeEnforcer, uten).first(), 0, 1)
        assertOutcome(getOutcomes(scopeEnforcer, tom).first(), 0, 1)

    }

    private fun assertOutcome(outcomes: Set<EnforcementOutcome>, expectedSuccess: Int, expectedFailure: Int) {
        val successful = outcomes.filter { it is Successful }
        val failure = outcomes.filter { it is Failure }
        assertEquals(expectedSuccess, successful.size)
        assertEquals(expectedFailure, failure.size)
    }

    private fun getOutcomes(
            enforcer: ClaimEnforcer,
            claims: Map<String, Claim>
    ) : Set<Set<EnforcementOutcome>> {
        return try {
            setOf(enforcer.enforce(claims))
        } catch (cause: ClaimEnforcementFailed) {
            cause.outcomes
        }
    }

    private fun getClaims(claims : String) : Map<String, Claim> {
        val encodedHeaders = Base64.getEncoder().encodeToString("{}".toByteArray())
        val encodedClaims = Base64.getEncoder().encodeToString(claims.toByteArray())
        val encodedSignature = Base64.getEncoder().encodeToString("im-a-signature".toByteArray())
        val token = "$encodedHeaders.$encodedClaims.$encodedSignature"
        return JWT.decode(token).claims
    }
}