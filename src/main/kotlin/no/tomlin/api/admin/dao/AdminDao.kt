package no.tomlin.api.admin.dao

import no.tomlin.api.admin.entity.Log
import no.tomlin.api.admin.entity.Visit
import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.Constants.TABLE_AIRLINE
import no.tomlin.api.common.Constants.TABLE_EPISODE
import no.tomlin.api.common.Constants.TABLE_LOCATION
import no.tomlin.api.common.Constants.TABLE_LOG
import no.tomlin.api.common.Constants.TABLE_MOVIE
import no.tomlin.api.common.Constants.TABLE_SEASON
import no.tomlin.api.common.Constants.TABLE_SETTINGS
import no.tomlin.api.common.Constants.TABLE_TRACK
import no.tomlin.api.common.Constants.TABLE_TV
import no.tomlin.api.common.Extensions.checkRowsAffected
import no.tomlin.api.common.PaginationResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun getStats(): Map<String, Int?> = mapOf(
        "movie" to countQuery(TABLE_MOVIE),
        "tv" to countQuery(TABLE_TV),
        "season" to countQuery(TABLE_SEASON),
        "episode" to countQuery(TABLE_EPISODE),
        "airline" to countQuery(TABLE_AIRLINE),
        "location" to countQuery(TABLE_LOCATION),
        "log" to countQuery(TABLE_LOG),
        "visit" to countQuery(TABLE_TRACK)
    )

    @Cacheable("settings")
    fun getSettings(): Map<String, Any?> =
        jdbcTemplate.query(
            "SELECT `key`, `value` FROM $TABLE_SETTINGS",
            EmptySqlParameterSource.INSTANCE
        ) { resultSet, _ ->
            resultSet.getString("key") to resultSet.getObject("value")
        }.toMap()

    fun getSetting(key: String): String? =
        jdbcTemplate.queryForObject(
            "SELECT `value` FROM $TABLE_SETTINGS WHERE `key` = :key",
            mapOf("key" to key),
            String::class.java
        )

    @CacheEvict("settings", allEntries = true)
    fun saveSetting(key: String, value: String?): Boolean =
        jdbcTemplate.update(
            "INSERT INTO $TABLE_SETTINGS (`key`, `value`) VALUES (:key, :value) ON DUPLICATE KEY UPDATE `value` = :value",
            mapOf("key" to key, "value" to value)
        ).checkRowsAffected()

    fun getLogs(page: Int): PaginationResponse<Log> {
        val start = (page - 1) * PAGE_SIZE

        val logs = jdbcTemplate.query(
            "SELECT * FROM $TABLE_LOG ORDER BY `timestamp` DESC LIMIT $PAGE_SIZE OFFSET $start"
        ) { resultSet, _ -> Log(resultSet) }

        val total = jdbcTemplate.queryForObject(
            "SELECT COUNT(id) total FROM $TABLE_LOG", EmptySqlParameterSource.INSTANCE, Int::class.java
        ) ?: 1

        return PaginationResponse(page, total, logs)
    }

    fun deleteLogs(): Int = jdbcTemplate.update("DELETE FROM $TABLE_LOG", EmptySqlParameterSource.INSTANCE)

    fun deleteLog(id: Long): Boolean = jdbcTemplate
        .update("DELETE FROM $TABLE_LOG WHERE `id` = :id", mapOf("id" to id))
        .checkRowsAffected()

    fun getVisits(page: Int): PaginationResponse<Visit> {
        val start = (page - 1) * PAGE_SIZE

        val visits = jdbcTemplate.query(
            "SELECT * FROM $TABLE_TRACK ORDER BY `visits` DESC LIMIT $PAGE_SIZE OFFSET $start"
        ) { resultSet, _ -> Visit(resultSet) }

        val total = jdbcTemplate.queryForObject(
            "SELECT COUNT(id) total FROM $TABLE_TRACK", EmptySqlParameterSource.INSTANCE, Int::class.java
        ) ?: 1

        return PaginationResponse(page, total, visits)
    }

    fun visit(ip: String, host: String?, referrer: String?, agent: String?, page: String?): Int =
        jdbcTemplate.update(
            "INSERT INTO $TABLE_TRACK (ip, host, referer, agent, page) " +
                "VALUES (:ip, :host, :referrer, :agent, :page) " +
                "ON DUPLICATE KEY UPDATE ip = :ip, host = :host, referer = :referrer, agent = :agent, page = :page, visits = visits + 1",
            mapOf(
                "ip" to ip,
                "host" to host,
                "referrer" to referrer,
                "agent" to agent,
                "page" to page
            )
        )

    private fun countQuery(table: String): Int? =
        jdbcTemplate.queryForObject("SELECT COUNT(id) FROM $table", EmptySqlParameterSource.INSTANCE, Int::class.java)
}
