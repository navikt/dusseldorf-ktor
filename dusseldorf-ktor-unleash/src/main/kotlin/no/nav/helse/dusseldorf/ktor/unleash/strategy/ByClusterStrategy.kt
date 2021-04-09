package no.nav.helse.dusseldorf.ktor.unleash.strategy

import no.finn.unleash.strategy.Strategy

class ByClusterStrategy(
    private val clusterName: String
) : Strategy {

    override fun getName(): String {
        return "byCluster"
    }

    override fun isEnabled(parameters: Map<String, String>): Boolean {
        return ToggleChecker.isToggleEnabled("cluster", parameters) { toggleClusterName: String ->
            clusterName.equals(toggleClusterName, ignoreCase = true)
        }
    }
}
