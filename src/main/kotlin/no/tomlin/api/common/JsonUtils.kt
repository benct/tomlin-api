package no.tomlin.api.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.PropertyNamingStrategy.LOWER_CAMEL_CASE
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

object JsonUtils {

    val jacksonMapper: ObjectMapper = jacksonObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .enable(JsonParser.Feature.ALLOW_TRAILING_COMMA)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModule(JavaTimeModule())
        .registerModule(ParameterNamesModule())
        .registerModule(Jdk8Module())

    inline fun <reified T : Any> String.parseJson(naming: PropertyNamingStrategy = LOWER_CAMEL_CASE): T =
        jacksonMapper.setPropertyNamingStrategy(naming).readValue(this, T::class.java)

    fun Any.toJson(naming: PropertyNamingStrategy = LOWER_CAMEL_CASE): String =
        jacksonMapper.setPropertyNamingStrategy(naming).writeValueAsString(this)
}
