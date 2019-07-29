package no.nav.helse.dusseldorf.ktor.auth

import com.auth0.jwt.impl.NullClaim
import com.auth0.jwt.interfaces.Claim

// https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
class AzureClaimRules {
    companion object {
        private enum class TokenVersion {
            V_1_0,
            V_2_0
        }

        private fun getTokenVersion(
                claims: Map<String, Claim>
        ) : TokenVersion {
            val version = claims["ver"]?.asString() ?: throw IllegalStateException("Ugyldig Azure Token. Inneholder ikke version claim 'ver'")
            return when (version) {
                "1.0" -> TokenVersion.V_1_0
                "2.0" -> TokenVersion.V_2_0
                else -> throw IllegalStateException("Azure token er på ikke støttet versjon '$version'.")
            }
        }

        private abstract class VersionVariableClaim(
                val claimMapping: Map<TokenVersion, String>) {
            fun getClaimName(claims: Map<String, Claim>) : String {
                val version = getTokenVersion(claims)
                return claimMapping[version]?: throw IllegalStateException("Feilkonfigurert VersionVariableClaim '${this.javaClass}'.")
            }
        }

        private class ClientIdClaim : VersionVariableClaim(mapOf(
                TokenVersion.V_1_0 to "appid",
                TokenVersion.V_2_0 to "azp"
        ))

        private class ClientAuthenticationModeClaim : VersionVariableClaim(mapOf(
                TokenVersion.V_1_0 to "appidacr",
                TokenVersion.V_2_0 to "azpacr"
        ))

        class EnforceAuthorizedClient(
                authorizedClients: Set<String>) : EnforceContainsOneOf(
                oneOf = authorizedClients) {
            override fun resolveClaimName(claims: Map<String, Claim>): String {
                return ClientIdClaim().getClaimName(claims )
            }
        }

        class EnforceCertificateClientAuthentication : EnforceEqualsOrContains(
                expected = "2") {
            override fun resolveClaimName(claims: Map<String, Claim>): String {
                return ClientAuthenticationModeClaim().getClaimName(claims )
            }
        }

        class EnforceInAllGroups(groups : Set<String>) : EnforceContainsAll(defaultClaimName = "groups", all = groups)
        class EnforceHasAllRoles(roles: Set<String>) : EnforceContainsAll(defaultClaimName = "roles", all = roles)
        class EnforceHasAllScopes(private val scopes: Set<String>) : ClaimRule {
            private companion object {
                private const val CLAIM = "scp"
                private const val DELIMITER = " "
            }
            override fun enforce(claims: Map<String, Claim>): EnforcementOutcome {
                val claimValue = claims[CLAIM]
                if (claimValue == null || claimValue is NullClaim) return Failure(CLAIM, scopes.joinToString(DELIMITER), null)
                val accessTokenScopes = claimValue.asString().split(DELIMITER).toSet()
                return if (accessTokenScopes.containsAll(scopes)) Successful(CLAIM, claimValue.asString())
                else Failure(CLAIM, scopes.joinToString(DELIMITER), accessTokenScopes.joinToString(DELIMITER))
            }
        }
    }
}