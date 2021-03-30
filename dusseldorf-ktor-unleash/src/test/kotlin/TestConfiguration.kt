object TestConfiguration {

    fun asMap(
        port : Int = 8080,
    ) : Map<String, String> {

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.unleash.app_id", "test-app"),
            Pair("nav.unleash.instance_id", "test-app-1"),
            Pair("nav.unleash.cluster", "test")
        )
        return map.toMap()
    }
}
