package no.nav.helse.dusseldorf.ktor.health

interface Result {
    fun result() : Map<String, Any?>
    companion object {
        fun merge(name: String, vararg results: Result) : Result {
            val healthy = mutableListOf<Map<String, Any?>>()
            val unHealthy = mutableListOf<Map<String, Any?>>()

            results.forEach { result -> when (result) {
                is Healthy -> healthy.add(result.result())
                else -> unHealthy.add(result.result())
            }}

            val merged = mapOf(
                "name" to name,
                "healthy" to healthy,
                "unhealthy" to unHealthy
            )

            return when (unHealthy.isEmpty()) {
                true -> Healthy(merged)
                false -> UnHealthy(merged)
            }
        }
    }
}

class Healthy(private val result : Map<String, Any?> ) : Result {
    constructor(name: String, result : Any) : this(mapOf("result" to result, "name" to name))

    override fun result(): Map<String, Any?> {
        return result
    }
}

class UnHealthy(private val result : Map<String, Any?> ) : Result {
    constructor(name: String, result : Any) : this(mapOf("result" to result, "name" to name))

    override fun result(): Map<String, Any?> {
        return result
    }
}

interface HealthCheck {
    suspend fun check() : Result
}

class TryCatchHealthCheck(private val name: String,
                          private val block: () -> Any) : HealthCheck {
    override suspend fun check(): Result {
        return try {
            block.invoke()
            Healthy(name = name, result = "Healthy!")
        } catch (cause: Throwable) {
            UnHealthy(name = name, result = if (cause.message == null) "Unhealthy!" else cause.message!!)
        }
    }
}