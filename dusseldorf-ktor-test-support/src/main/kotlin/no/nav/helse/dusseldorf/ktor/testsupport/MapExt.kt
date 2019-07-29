package no.nav.helse.dusseldorf.ktor.testsupport

fun Map<String, String>.asArguments() : Array<String>  {
    val list = mutableListOf<String>()
    forEach { configKey, configValue ->
        list.add("-P:$configKey=$configValue")
    }
    return list.toTypedArray()
}