package no.tomlin.api.finn

import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.queryForList
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.TABLE_FINN
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class FinnDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun get(): List<Map<String, Any?>> = jdbc.queryForList(
        Select(from = TABLE_FINN, orderBy = OrderBy("id" to "DESC", "timestamp" to "DESC"))
    )

    fun get(id: Long): List<Map<String, Any?>> = jdbc.queryForList(
        Select(from = TABLE_FINN, where = Where("id" to id), orderBy = OrderBy("timestamp" to "DESC"))
    )

    fun getUniqueIds(): List<Long> = jdbc.queryForList(
        Select(columns = "DISTINCT(id)", from = TABLE_FINN),
        Long::class.java
    )

    fun save(id: Long, price: String): Boolean = jdbc.update(
        Insert(TABLE_FINN, mapOf("id" to id, "price" to price))
    )

    fun delete(id: Long): Boolean = jdbc.update(
        Delete(TABLE_FINN, Where("id" to id))
    )
}
