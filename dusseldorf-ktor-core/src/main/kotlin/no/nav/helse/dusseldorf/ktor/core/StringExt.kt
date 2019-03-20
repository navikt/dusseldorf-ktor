package no.nav.helse.dusseldorf.ktor.core

import java.net.URL

fun String.fromResources() : URL = Thread.currentThread().contextClassLoader.getResource(this)
