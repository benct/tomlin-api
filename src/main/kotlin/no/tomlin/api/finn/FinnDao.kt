package no.tomlin.api.finn

import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.queryForList
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Insert
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.TABLE_FINN
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class FinnDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun get(): List<Map<String, Any?>> = jdbc.queryForList(
        Select(TABLE_FINN)
            .orderBy("id" to "DESC", "timestamp" to "DESC")
    )

    fun get(id: Long): List<Map<String, Any?>> = jdbc.queryForList(
        Select(TABLE_FINN)
            .where("id").eq(id)
            .orderBy("timestamp" to "DESC")
    )

    fun getUniqueIds(): List<Long> = jdbc.queryForList(
        Select(TABLE_FINN).column("id").distinct(),
        Long::class.java
    )

    fun save(id: Long, price: String): Boolean = jdbc.update(
        Insert(TABLE_FINN).data("id" to id, "price" to price)
    )

    fun delete(id: Long): Boolean = jdbc.update(
        Delete(TABLE_FINN).where("id").eq(id)
    )
}
