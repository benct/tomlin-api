package no.tomlin.api.common

object Extensions {

    fun String?.required(field: String): String = this ?: throw IllegalArgumentException("$field cannot be null")

    fun List<String>.cleanBlank() = this.map { if (it.isNotBlank()) it.trim() else null }
}
