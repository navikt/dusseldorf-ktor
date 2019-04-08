package no.nav.helse.dusseldorf.ktor.core

import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException
import java.time.Duration
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals


class RetryTest {
    @Test
    fun `Feil på første forsøk og OK på neste førsk gir OK response`() {
        val failing = Failing(1)
        val response = runBlocking {
            Retry.retry(
                    tries = 2,
                    operation = "Feil på første forsøk og OK på neste førsøk gir OK response"
            ) {
                failing.call()
            }
        }
        assertEquals("OK", response)
    }

    @Test(expected = IllegalStateException::class)
    fun `Feiler på siste forsøk gir originale exception`() {
        val failing = Failing(5)
        runBlocking {
            Retry.retry(
                    tries = 5,
                    operation = "Feiler på siste forsøk gir originale exception"
            ) {
                failing.call()
            }
        }
    }

    @Test
    fun `OK på første forsøk`() {
        val failing = Failing(0)
        runBlocking {
            Retry.retry(
                    tries = 5,
                    operation = "OK på første forsøk"
            ) {
                failing.call()
            }
        }
    }

}

private class Failing(
        private val numberOfFailsBeforeOk : Int
) {
    private var calls = 0

    fun call() : String{
        if (++calls <= numberOfFailsBeforeOk) {
            throw IllegalStateException("I am now failing")
        }
        return "OK"

    }
}