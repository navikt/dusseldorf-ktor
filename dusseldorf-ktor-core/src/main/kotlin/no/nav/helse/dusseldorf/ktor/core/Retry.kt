package no.nav.helse.dusseldorf.ktor.core

import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class Retry {
    companion object {
        suspend fun <T> retry(
                tries: Int = 3,
                operation: String,
                initialDelay: Duration = Duration.ofMillis(100),
                maxDelay: Duration = Duration.ofSeconds(1),
                factor: Double = 2.0,
                logger: Logger = LoggerFactory.getLogger("no.nav.helse.dusseldorf.ktor.core.Retry"),
                exceptionHandler: (Throwable) -> Unit = {},
                block: suspend () -> T): T
        {
            var currentDelayInMillis = initialDelay.toMillis()
            repeat(tries - 1) {
                val currentTry = it + 1
                logger.startForsok(operation = operation, currentTry = currentTry, tries = tries)
                try {
                    val result = block()
                    logger.gikkOk(operation = operation, currentTry = currentTry, tries = tries)
                    return result
                } catch (cause: Throwable) {
                    logger.warn("Feil ved '$operation'. ${cause.javaClass.simpleName} med melding '${cause.message}'")
                    exceptionHandler(cause)
                }
                logger.warn("Venter $currentDelayInMillis millisekunder før neste forsøk.")
                delay(currentDelayInMillis)
                currentDelayInMillis = (currentDelayInMillis * factor).toLong().coerceAtMost(maxDelay.toMillis())
            }
            // Eventuelt siste forsøk
            logger.startForsok(operation = operation, currentTry = tries, tries = tries)
            val result=  block()
            logger.gikkOk(operation = operation, currentTry = tries, tries = tries)
            return result
        }
    }
}

private fun Logger.startForsok(
        operation: String,
        currentTry : Int,
        tries: Int) {
    val log = "Forsøker '$operation' forsøk $currentTry av $tries."
    if (currentTry > 1) warn(log)
    else trace(log)
}


private fun Logger.gikkOk(
        operation: String,
        currentTry : Int,
        tries: Int) {
    val log = "'$operation' gikk bra på forsøk $currentTry av $tries."
    if (currentTry > 1) info(log)
    else trace(log)
}
