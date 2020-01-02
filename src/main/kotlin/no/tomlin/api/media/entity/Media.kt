package no.tomlin.api.media.entity

abstract class Media {

    abstract val table: String

    abstract val keys: Set<String>

    fun insertStatement(updated: Boolean = true): String =
        "INSERT INTO $table (${keys.joinToString { "`${it}`" }}) VALUES (${keys.joinToString { ":$it" }}) " +
            "ON DUPLICATE KEY UPDATE ${keys.joinToString { "`${it}` = :${it}" }}" +
            if (updated) ", `updated` = CURRENT_TIMESTAMP" else ""

    fun updateStatement(): String = "UPDATE $table SET ${keys.joinToString { "`${it}` = :${it}" }} WHERE `id` = :id"

    fun deleteStatement(): String = "DELETE FROM $table WHERE `id` = :id"
}
