package no.nav.helse.dusseldorf.ktor.unleash.strategy

import no.finn.unleash.strategy.Strategy
import no.nav.helse.dusseldorf.ktor.core.EnvironmentUtils

class ByClusterStrategy : Strategy {
    override fun getName(): String {
        return "byCluster"
    }

    override fun isEnabled(parameters: Map<String, String>): Boolean {
        return ToggleChecker.isToggleEnabled("cluster", parameters) { toggleClusterName: String ->
            isCluster(
                toggleClusterName
            )
        }
    }

    companion object {
        private fun isCluster(toggleClusterName: String): Boolean {
            val clusterName = EnvironmentUtils.clusterName ?: "NO_CLUSTER_NAME"
            return clusterName.equals(toggleClusterName, ignoreCase = true)
        }
    }
}
