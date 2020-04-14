package no.nav.helse.dusseldorf.ktor.streams

data class Steg(
        val navn: String,
        val topic: String,
        val forrige: Steg?,
        val dlqTopic: String? = null
)