package no.nav.helse.dusseldorf.ktor.auth

private const val AUDIENCE_CLAIM = "aud"
private const val SUBJECT_CLAIM = "sub"

class StandardClaimRules {
    companion object {
        class EnforceAudienceEquals(requiredAudience: String) : EnforceEqualsOrContains(AUDIENCE_CLAIM, requiredAudience)
        class EnforceSubjectOneOf(authorizedSubjects: Set<String>) : EnforceContainsOneOf(SUBJECT_CLAIM, authorizedSubjects)
    }
}