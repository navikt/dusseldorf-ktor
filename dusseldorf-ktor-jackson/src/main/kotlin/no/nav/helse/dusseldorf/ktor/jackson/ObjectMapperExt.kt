package no.nav.helse.dusseldorf.ktor.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

fun ObjectMapper.dusseldorfConfigured() : ObjectMapper {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    registerModule(JavaTimeModule())
    return this
}