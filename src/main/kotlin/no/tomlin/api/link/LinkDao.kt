package no.tomlin.api.link

import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.TABLE_LINK
import no.tomlin.api.db.Update
import no.tomlin.api.db.Upsert
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class LinkDao(private val jdbc: NamedParameterJdbcTemplate) {

    @Cacheable("link")
    fun get(): List<Link> = jdbc.query(
        Select(TABLE_LINK)
            .orderBy("order" to "ASC", "id" to "ASC"),
        Link.rowMapper,
    )

    fun get(id: Long): Link? = jdbc.queryForObject(
        Select(TABLE_LINK)
            .where("id").eq(id),
        Link.rowMapper,
    )

    @CacheEvict("link", allEntries = true)
    fun save(id: Long?, title: String, href: String, icon: String, target: String?, private: Boolean): Boolean =
        jdbc.update(
            Upsert(TABLE_LINK)
                .data(
                    "id" to id,
                    "title" to title,
                    "href" to href,
                    "icon" to icon,
                    "target" to target,
                    "private" to private,
                )
        )

    @CacheEvict("link", allEntries = true)
    fun setOrder(id: Long, order: Int): Boolean = jdbc.update(
        Update(TABLE_LINK)
            .set("order" to order)
            .where("id").eq(id)
    )

    @CacheEvict("link", allEntries = true)
    fun delete(id: Long): Boolean = jdbc.update(
        Delete(TABLE_LINK)
            .where("id").eq(id)
    )

    data class Link(
        val id: Long,
        val title: String,
        val href: String,
        val icon: String,
        val target: String?,
        val private: Boolean,
        val order: Int,
        val visited: Int,
        val lastVisit: LocalDateTime,
    ) {
        companion object {
            val rowMapper = RowMapper<Link> { rs, _ ->
                Link(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("href"),
                    rs.getString("icon"),
                    rs.getString("target"),
                    rs.getBoolean("private"),
                    rs.getInt("order"),
                    rs.getInt("visited"),
                    rs.getTimestamp("last_visit").toLocalDateTime(),
                )
            }
        }
    }

}
