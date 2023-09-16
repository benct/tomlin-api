package no.tomlin.api.qrator

import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.queryForList
import no.tomlin.api.db.Extensions.queryForMap
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.TABLE_QRATOR
import no.tomlin.api.db.Update
import no.tomlin.api.db.Where
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class QratorDao(private val jdbc: NamedParameterJdbcTemplate, private val jdbcInsert: JdbcTemplate) {

    fun get(): List<Map<String, Any?>> = jdbc.queryForList(
        Select(TABLE_QRATOR)
    )

    fun get(id: Long): Map<String, Any?> = jdbc.queryForMap(
        Select(from = TABLE_QRATOR, where = Where("id" to id))
    )

    fun create(extension: String?): Long? {
        val keyHolder = GeneratedKeyHolder()

        jdbcInsert.update({ connection ->
            val ps = connection.prepareStatement(
                "INSERT INTO $TABLE_QRATOR (`ext`) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setString(1, extension)
            ps
        }, keyHolder)

        return keyHolder.key?.toLong()
    }

    fun update(id: Long, title: String?, author: String?, value: Int?, description: String?): Boolean = jdbc.update(
        Update(
            TABLE_QRATOR,
            mapOf(
                "title" to title,
                "author" to author,
                "value" to value,
                "description" to description,
            ),
            Where("id" to id),
        )
    )

    fun delete(id: Long): Boolean = jdbc.update(
        Delete(TABLE_QRATOR, Where("id" to id))
    )
}