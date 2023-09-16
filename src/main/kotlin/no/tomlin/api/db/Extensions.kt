package no.tomlin.api.db

import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

object Extensions {

    fun <T> NamedParameterJdbcTemplate.query(select: Select, rowMapper: RowMapper<T>): List<T> =
        query(select.statement, select.data, rowMapper)

    fun <T> NamedParameterJdbcTemplate.queryForObject(select: Select, rowMapper: RowMapper<T>): T? =
        queryForObject(select.statement, select.data, rowMapper)

    fun <T> NamedParameterJdbcTemplate.queryForObject(select: Select, type: Class<T>): T? =
        queryForObject(select.statement, select.data, type)

    fun <T> NamedParameterJdbcTemplate.queryForList(select: Select, type: Class<T>): List<T> =
        queryForList(select.statement, select.data, type)

    fun NamedParameterJdbcTemplate.queryForList(select: Select): List<MutableMap<String, Any?>> =
        queryForList(select.statement, select.data)

    fun NamedParameterJdbcTemplate.queryForMap(select: Select): MutableMap<String, Any?> =
        queryForMap(select.statement, select.data)

    fun NamedParameterJdbcTemplate.update(insert: Insert): Boolean =
        update(insert.statement, insert.data).checkRowsAffected(insert.statement)

    fun NamedParameterJdbcTemplate.update(upsert: Upsert): Boolean =
        update(upsert.statement, upsert.data).checkRowsAffected(upsert.statement)

    fun NamedParameterJdbcTemplate.update(update: Update): Boolean =
        update(update.statement, update.data).checkRowsAffected(update.statement)

    fun NamedParameterJdbcTemplate.update(delete: Delete): Boolean =
        update(delete.statement, delete.data).checkRowsAffected(delete.statement)

    fun NamedParameterJdbcTemplate.update(increment: Increment): Boolean =
        update(increment.statement, increment.data).checkRowsAffected(increment.statement)

    fun NamedParameterJdbcTemplate.update(decrement: Decrement): Boolean =
        update(decrement.statement, decrement.data).checkRowsAffected(decrement.statement)

    private fun Int.checkRowsAffected(sql: String, expected: Int = 1): Boolean =
        if (this < expected) throw JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, expected, this) else true
}