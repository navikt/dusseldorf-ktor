package no.nav.helse.dusseldorf.ktor.unleash

import com.silvercar.unleash.strategy.Strategy

class IsNotProdStrategy : Strategy {
    private val isProd: Boolean

    constructor() {
        isProd = DEFAULT_PROD_ENVIRONMENTS.contains(System.getenv("NAIS_CLUSTER_NAME")) ||
                DEFAULT_PROD_ENVIRONMENTS.contains(System.getenv("FASIT_ENVIRONMENT_NAME"))
    }

    constructor(currentEnvironment: String, prodEnvironment: String?) {
        isProd = currentEnvironment.equals(prodEnvironment, ignoreCase = true)
    }

    override val name: String
        get() = "isNotProd"

    override fun isEnabled(parameters: Map<String, String>): Boolean {
        return !isProd
    }

    companion object {
        private val DEFAULT_PROD_ENVIRONMENTS: List<String> = listOf("prod-sbs", "prod-fss", "p", "prod-gcp")
    }
}
