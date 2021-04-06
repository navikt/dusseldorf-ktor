object TestConfiguration {

    fun asMap(
        port : Int = 8080,
        cluster: String = "test"
    ) : Map<String, String> {

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.unleash.app_name", "dusseldorf-ktor-unleash-test-app"),
            Pair("nav.unleash.instance_id", "dusseldorf-ktor-unleash-test-app-1"),
            Pair("nav.unleash.api_url", "https://unleash.nais.io/api/"),
            Pair("nav.unleash.cluster", cluster)
        )
        return map.toMap()
    }
}
