package no.tomlin.api.common

import java.time.Duration

object Extensions {

    fun String.ifNotEmpty(defaultValue: (String) -> String): String =
        if (isNotEmpty()) defaultValue(this) else this

    fun String.ifNotBlank(defaultValue: (String) -> String): String =
        if (isNotBlank()) defaultValue(this) else this

    fun String?.required(field: String): String = this ?: throw IllegalArgumentException("$field cannot be null")

    fun String?.nullIfBlank(): String? = this?.ifBlank { null }

    fun List<String>.nullIfBlank(): List<String?> = this.map { if (it.isBlank()) null else it.trim() }

    fun Long.formatDuration(): String =
        Duration.ofMillis(this).let { "${it.toHoursPart()}h ${it.toMinutesPart()}m ${it.toSecondsPart()}s" }
}
