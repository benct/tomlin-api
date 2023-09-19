package no.tomlin.api.admin.dao

import no.tomlin.api.admin.entity.Log
import no.tomlin.api.admin.entity.Visit
import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table
import no.tomlin.api.db.Table.*
import no.tomlin.api.db.Upsert
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
        Select(TABLE_SETTINGS).columns("key", "value")
    ) { resultSet, _ ->
        resultSet.getString("key") to resultSet.getObject("value")
    }.toMap()

    fun getSetting(key: String): String? = jdbc.queryForObject(
        Select(TABLE_SETTINGS)
            .columns("value")
            .where("key").eq(key),
        String::class.java
    )

    @CacheEvict("settings", allEntries = true)
    fun saveSetting(key: String, value: String?): Boolean = jdbc.update(
        Upsert(TABLE_SETTINGS).data("key" to key, "value" to value)
    )

    fun getLogs(page: Int): PaginationResponse<Log> {
        val start = (page - 1) * PAGE_SIZE

        val logs = jdbc.query(
            Select(TABLE_LOG).orderBy("timestamp" to "DESC").limit(PAGE_SIZE, offset = start),
            Log.rowMapper,
        )

        val total = jdbc.queryForObject(
            Select(TABLE_LOG).column("id").count(),
            Int::class.java,
        ) ?: 1

        return PaginationResponse(page, total, logs)
    }

    fun deleteLogs(): Boolean = jdbc.update(Delete(TABLE_LOG))

    fun deleteLog(id: Long): Boolean = jdbc.update(Delete(TABLE_LOG).where("id").eq(id))

    fun getVisits(page: Int): PaginationResponse<Visit> {
        val start = (page - 1) * PAGE_SIZE

        val visits = jdbc.query(
            Select(TABLE_TRACK).orderBy("visits" to "DESC").limit(PAGE_SIZE, offset = start),
            Visit.rowMapper,
        )

        val total = jdbc.queryForObject(
            Select(TABLE_TRACK).column("id").count(),
            Int::class.java,
        ) ?: 1

        return PaginationResponse(page, total, visits)
    }

    fun visit(ip: String, host: String?, referer: String?, agent: String?, page: String?): Boolean = jdbc.update(
        Upsert(TABLE_TRACK)
            .data("ip" to ip, "host" to host, "referer" to referer, "agent" to agent, "page" to page)
            .incrementOnUpdate("visits")
    )

    private fun countQuery(table: Table): Int? = jdbc.queryForObject(
        Select(table).column("id").count(),
        Int::class.java
    )
}
