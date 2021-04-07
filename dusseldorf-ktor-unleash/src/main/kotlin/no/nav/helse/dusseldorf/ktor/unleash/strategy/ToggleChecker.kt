package no.nav.helse.dusseldorf.ktor.unleash.strategy

import java.util.*
import java.util.function.Predicate
import java.util.stream.Stream

object ToggleChecker {
    fun isToggleEnabled(
        parameterName: String, parameters: Map<String, String>,
        checkIsEnabled: Predicate<String>
    ): Boolean {
        return Optional.ofNullable(parameters)
            .map { m: Map<String, String> -> m[parameterName] }
            .map { s: String? -> s!!.split(",".toRegex()).toTypedArray() }
            .map { array: Array<String>? -> Arrays.stream(array) }
            .map { s: Stream<String> -> s.anyMatch(checkIsEnabled) }
            .orElse(false)
    }
}
