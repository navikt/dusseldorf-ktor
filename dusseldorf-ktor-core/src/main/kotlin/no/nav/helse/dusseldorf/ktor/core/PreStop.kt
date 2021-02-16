package no.nav.helse.dusseldorf.ktor.core

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.helse.dusseldorf.ktor.core.PreStop.performPreStop
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

private object PreStop {
    private const val EtMinutt = 60000L
    private val logger: Logger = LoggerFactory.getLogger(PreStop::class.java)

    suspend fun List<PreStopAction>.performPreStop() {
        logger.info("Starter pre-stop")
        withTimeout(EtMinutt) {
            forEach { preStopAction -> runCatching {
                preStopAction.preStop()
            }.onFailure { logger.warn("Feil ved pre-stop ${preStopAction.javaClass.simpleName}", it) }}
        }
        logger.info("Ferdig med pre-stop")
    }
}

fun Application.preStopOnApplicationStopPreparing(
    preStopActions: List<PreStopAction>) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        runBlocking { preStopActions.performPreStop() }
    }
}

fun Route.PreStopRoute(
    preStopActions: List<PreStopAction>) {
    get(Paths.PRE_STOP_PATH) {
        preStopActions.performPreStop()
        call.respondText("STOPPED")
    }
}

interface PreStopAction {
    suspend fun preStop()
}

class FullførAktiveRequester(
    application: Application,
    private val ignorePaths: Set<String> = Paths.DEFAULT_EXCLUDED_PATHS
) : PreStopAction {

    private fun ApplicationRequest.skalTelles() : Boolean {
        val erInternal = path().startsWith("/internal")
        val erIgnorert = ignorePaths.contains(path())
        return !erInternal && !erIgnorert
    }

    private fun Application.tellAntallAktiveRequester() {
        environment.monitor.subscribe(Routing.RoutingCallStarted) {
            if (it.request.skalTelles()) {
                antallAktiveRequester.incrementAndGet()
            }
        }
        environment.monitor.subscribe(Routing.RoutingCallFinished) {
            if (it.request.skalTelles()) {
                antallAktiveRequester.decrementAndGet()
            }
        }
    }

    init {
        application.tellAntallAktiveRequester()
    }

    override suspend fun preStop() {
        val antallAktiveFør = antallAktiveRequester.get()
        logger.info("Antall aktive requester før pre-stop $antallAktiveFør")
        withTimeout(TyveSekunder) {
            while (antallAktiveRequester.get() > 0) {
                delay(HalvtSekund)
            }
        }
        val antallAktiveEtter = antallAktiveRequester.get()
        "Antall aktive requester etter pre-stop $antallAktiveEtter".also {
            if (antallAktiveEtter > 0) { logger.warn(it) }
            else { logger.info(it) }
        }
    }

    private companion object {
        private const val TyveSekunder = 20000L
        private const val HalvtSekund = 500L
        private val antallAktiveRequester = AtomicInteger(0)
        private val logger: Logger = LoggerFactory.getLogger(FullførAktiveRequester::class.java)
    }
}
