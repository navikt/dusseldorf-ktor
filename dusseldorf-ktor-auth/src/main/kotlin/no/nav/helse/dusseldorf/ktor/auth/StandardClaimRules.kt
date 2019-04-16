package no.nav.helse.dusseldorf.ktor.auth

private const val AUDIENCE_CLAIM = "aud"

class StandardClaimRules {
    companion object {
        class EnforceAudience(requiredAudience: String) : EnforceEqualsOrContains(AUDIENCE_CLAIM, requiredAudience)
    }
}