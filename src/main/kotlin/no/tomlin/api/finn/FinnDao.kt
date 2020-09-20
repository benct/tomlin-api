package no.tomlin.api.finn

import no.tomlin.api.common.Constants.TABLE_FINN
import no.tomlin.api.common.Extensions.checkRowsAffected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class FinnDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun get(): List<Map<String, Any>> = jdbcTemplate
        .queryForList("SELECT * FROM $TABLE_FINN ORDER BY `id` DESC, `timestamp` DESC", EmptySqlParameterSource.INSTANCE)

    fun get(id: Long): List<Map<String, Any?>> = jdbcTemplate
        .queryForList("SELECT * FROM $TABLE_FINN WHERE `id` = :id ORDER BY `timestamp` DESC", mapOf("id" to id))

    fun getUniqueIds(): List<Long> = jdbcTemplate
        .queryForList("SELECT DISTINCT(`id`) FROM $TABLE_FINN", EmptySqlParameterSource.INSTANCE, Long::class.java)

    fun save(id: Long, price: String): Boolean = jdbcTemplate
        .update("INSERT INTO $TABLE_FINN (id, price) VALUES (:id, :price)", mapOf("id" to id, "price" to price))
        .checkRowsAffected()

    fun delete(id: Long): Boolean = jdbcTemplate
        .update("DELETE FROM $TABLE_FINN WHERE id = :id", mapOf("id" to id))
        .checkRowsAffected()
}
