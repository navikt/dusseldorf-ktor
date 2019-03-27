package no.nav.helse.dusseldorf.ktor.health

interface Result {
    fun result() : Map<String, Any?>
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