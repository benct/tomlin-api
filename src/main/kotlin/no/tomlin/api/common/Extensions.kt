package no.tomlin.api.common

import java.time.Duration

object Extensions {

    fun String?.required(field: String): String = this ?: throw IllegalArgumentException("$field cannot be null")

    fun String?.nullIfBlank(): String? = this?.let { if (it.isNotBlank()) it else null }

    fun List<String>.nullIfBlank(): List<String?> = this.map { if (it.isBlank()) null else it.trim() }

    fun Int.checkRowsAffected(): Boolean =
        if (this !in 1..2) throw AffectedIncorrectNumberOfRowsException(this) else true

    fun Long.formatDuration(): String =
        Duration.ofMillis(this).let { "${it.toHoursPart()}h ${it.toMinutesPart()}m ${it.toSecondsPart()}s" }
}
