package no.tomlin.api.admin.dao

import no.tomlin.api.admin.entity.Log
import no.tomlin.api.admin.entity.Visit
import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun getStats(): Map<String, Int?> = mapOf(
        "movie" to countQuery(TABLE_MOVIE),
        "tv" to countQuery(TABLE_TV),
        "season" to countQuery(TABLE_SEASON),
        "episode" to countQuery(TABLE_EPISODE),
        "airline" to countQuery(TABLE_IATA_AIRLINE),
        "location" to countQuery(TABLE_IATA_LOCATION),
        "log" to countQuery(TABLE_LOG),
        "visit" to countQuery(TABLE_TRACK)
    )

    @Cacheable("settings")
    fun getSettings(): Map<String, Any?> = jdbc.query(
        Select(columns = "`key`, `value`", from = TABLE_SETTINGS)
    ) { resultSet, _ ->
        resultSet.getString("key") to resultSet.getObject("value")
    }.toMap()

    fun getSetting(key: String): String? = jdbc.queryForObject(
        Select(columns = "`value`", from = TABLE_SETTINGS, where = Where("key" to key)),
        String::class.java
    )

    @CacheEvict("settings", allEntries = true)
    fun saveSetting(key: String, value: String?): Boolean = jdbc.update(
        Upsert(TABLE_SETTINGS, mapOf("key" to key, "value" to value))
    )

    fun getLogs(page: Int): PaginationResponse<Log> {
        val start = (page - 1) * PAGE_SIZE

        val logs = jdbc.query(
            Select(from = TABLE_LOG, orderBy = OrderBy("timestamp" to "DESC"), limit = PAGE_SIZE, offset = start),
            Log.rowMapper,
        )

        val total = jdbc.queryForObject(
            Select(columns = "COUNT(id)", from = TABLE_LOG),
            Int::class.java,
        ) ?: 1

        return PaginationResponse(page, total, logs)
    }

    fun deleteLogs(): Boolean = jdbc.update(Delete(TABLE_LOG))

    fun deleteLog(id: Long): Boolean = jdbc.update(Delete(TABLE_LOG, Where("id" to id)))

    fun getVisits(page: Int): PaginationResponse<Visit> {
        val start = (page - 1) * PAGE_SIZE

        val visits = jdbc.query(
            Select(from = TABLE_TRACK, orderBy = OrderBy("visits" to "DESC"), limit = PAGE_SIZE, offset = start),
            Visit.rowMapper,
        )

        val total = jdbc.queryForObject(
            Select(columns = "COUNT(id)", from = TABLE_TRACK),
            Int::class.java,
        ) ?: 1

        return PaginationResponse(page, total, visits)
    }

    fun visit(ip: String, host: String?, referer: String?, agent: String?, page: String?): Boolean = jdbc.update(
        Upsert(
            TABLE_TRACK,
            mapOf("ip" to ip, "host" to host, "referer" to referer, "agent" to agent, "page" to page),
            incrementOnDuplicate = "visits"
        )
    )

    private fun countQuery(table: Table): Int? = jdbc.queryForObject(
        Select(columns = "COUNT(id)", from = table),
        Int::class.java
    )
}
