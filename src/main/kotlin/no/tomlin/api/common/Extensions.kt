package no.tomlin.api.common

object Extensions {

    fun String?.required(field: String): String = this ?: throw IllegalArgumentException("$field cannot be null")

    fun String?.nullIfBlank() = this?.let { if (it.isNotBlank()) it else null }

    fun List<String>.nullIfBlank() = this.map { if (it.isBlank()) null else it.trim() }
}
