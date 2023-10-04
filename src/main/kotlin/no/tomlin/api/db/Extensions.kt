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

    fun <T> NamedParameterJdbcTemplate.update(sql: Statement<T>, minimumRowsAffected: Int = 1): Boolean =
        update(sql.statement, sql.data).checkRowsAffected(sql.statement, minimumRowsAffected)

    private fun Int.checkRowsAffected(sql: String, expected: Int = 1): Boolean =
        if (this < expected) throw JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, expected, this) else true
}