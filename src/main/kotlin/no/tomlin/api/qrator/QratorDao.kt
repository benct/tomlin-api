package no.tomlin.api.qrator

import no.tomlin.api.common.Constants.TABLE_QRATOR
import no.tomlin.api.common.Extensions.checkRowsAffected
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class QratorDao(private val jdbcTemplate: NamedParameterJdbcTemplate, private val insertTemplate: JdbcTemplate) {

    fun get(): List<Map<String, Any>> =
        jdbcTemplate.queryForList("SELECT * FROM $TABLE_QRATOR", EmptySqlParameterSource.INSTANCE)

    fun get(id: Long): Map<String, Any?> =
        jdbcTemplate.queryForMap("SELECT * FROM $TABLE_QRATOR WHERE `id` = :id", mapOf("id" to id))

    fun create(extension: String?): Long? {
        val keyHolder = GeneratedKeyHolder()

        insertTemplate.update({ connection ->
            val ps = connection.prepareStatement(
                "INSERT INTO $TABLE_QRATOR (`ext`) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setString(1, extension)
            ps
        }, keyHolder)

        return keyHolder.key?.toLong()
    }

    fun update(id: Long, title: String?, author: String?, value: Int?, description: String?): Boolean =
        jdbcTemplate.update(
            "UPDATE $TABLE_QRATOR " +
                "SET `title` = :title, `author` = :author, `value` = :value, `description` = :description " +
                "WHERE `id` = :id",
            mapOf(
                "id" to id,
                "title" to title,
                "author" to author,
                "value" to value,
                "description" to description,
            )
        ).checkRowsAffected()

    fun delete(id: Long): Boolean =
        jdbcTemplate.update("DELETE FROM $TABLE_QRATOR WHERE id = :id", mapOf("id" to id)).checkRowsAffected()
}