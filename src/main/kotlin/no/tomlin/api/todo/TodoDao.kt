package no.tomlin.api.todo

import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.TABLE_TODO
import no.tomlin.api.db.Update
import no.tomlin.api.db.Upsert
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class TodoDao(private val jdbc: NamedParameterJdbcTemplate) {

    @Cacheable("todo")
    fun get(): List<Todo> = jdbc.query(
        Select(TABLE_TODO)
            .orderBy("order" to "ASC", "id" to "DESC"),
        Todo.rowMapper,
    )

    fun get(id: Long): Todo? = jdbc.queryForObject(
        Select(TABLE_TODO)
            .where("id").eq(id),
        Todo.rowMapper,
    )

    @CacheEvict("todo", allEntries = true)
    fun save(id: Long?, title: String, text: String?, private: Boolean): Boolean = jdbc.update(
        Upsert(TABLE_TODO)
            .data("id" to id, "title" to title, "text" to text)
    )

    @CacheEvict("todo", allEntries = true)
    fun setChecked(id: Long, checked: Boolean): Boolean = jdbc.update(
        Update(TABLE_TODO)
            .set("checked" to checked)
            .where("id").eq(id)
    )

    @CacheEvict("todo", allEntries = true)
    fun setOrder(id: Long, order: Int): Boolean = jdbc.update(
        Update(TABLE_TODO)
            .set("order" to order)
            .where("id").eq(id)
    )

    @CacheEvict("todo", allEntries = true)
    fun delete(id: Long): Boolean = jdbc.update(
        Delete(TABLE_TODO)
            .where("id").eq(id)
    )

    data class Todo(
        val id: Long,
        val title: String,
        val text: String?,
        val checked: Boolean,
        val private: Boolean,
        val order: Int,
        val created: LocalDateTime,
    ) {
        companion object {
            val rowMapper = RowMapper<Todo> { rs, _ ->
                Todo(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("text"),
                    rs.getBoolean("checked"),
                    rs.getBoolean("private"),
                    rs.getInt("order"),
                    rs.getTimestamp("created").toLocalDateTime()
                )
            }
        }
    }

}
