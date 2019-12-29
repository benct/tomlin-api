package no.tomlin.api.common

object Extensions {

    fun String?.required(field: String): String = this ?: throw IllegalArgumentException("$field cannot be null")

    fun String?.nullIfBlank() = this?.let { if (it.isNotBlank()) it else null }

    fun List<String>.cleanBlank() = this.map { if (it.isNotBlank()) it.trim() else null }
}
