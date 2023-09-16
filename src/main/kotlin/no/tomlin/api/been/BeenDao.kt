package no.tomlin.api.been

import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.TABLE_BEEN
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class BeenDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun get(): List<Map<String, Any>> = jdbc.query(
        Select(TABLE_BEEN)
    ) { resultSet, _ ->
        mapOf(
            "country" to resultSet.getString("country"),
            "name" to resultSet.getString("name"),
            "visited" to resultSet.getInt("visited"),
        )
    }

    fun add(country: String, name: String): Boolean = jdbc.update(
        Insert(TABLE_BEEN, mapOf("country" to country, "name" to name))
    )

    fun remove(country: String): Boolean = jdbc.update(
        Delete(TABLE_BEEN, Where("country" to country))
    )

    fun increment(country: String): Boolean = jdbc.update(
        Increment(TABLE_BEEN, column = "visited", Where("country" to country))
    )

    fun decrement(country: String): Boolean = jdbc.update(
        Decrement(TABLE_BEEN, column = "visited", Where("country" to country))
    )
}
