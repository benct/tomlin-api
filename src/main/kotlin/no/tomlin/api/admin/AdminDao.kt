package no.tomlin.api.admin

import no.tomlin.api.common.Constants.TABLE_AIRLINE
import no.tomlin.api.common.Constants.TABLE_EPISODE
import no.tomlin.api.common.Constants.TABLE_HASS
import no.tomlin.api.common.Constants.TABLE_LOCATION
import no.tomlin.api.common.Constants.TABLE_LOG
import no.tomlin.api.common.Constants.TABLE_MOVIE
import no.tomlin.api.common.Constants.TABLE_NOTE
import no.tomlin.api.common.Constants.TABLE_SEASON
import no.tomlin.api.common.Constants.TABLE_TRACK
import no.tomlin.api.common.Constants.TABLE_TV
import no.tomlin.api.entity.Log
import no.tomlin.api.entity.Note
import no.tomlin.api.entity.Visit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class AdminDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun getStats(): Map<String, Int?> = mapOf(
        "movie" to countQuery(TABLE_MOVIE),
        "tv" to countQuery(TABLE_TV),
        "season" to countQuery(TABLE_SEASON),
        "episode" to countQuery(TABLE_EPISODE),
        "hass" to countQuery(TABLE_HASS),
        "airline" to countQuery(TABLE_AIRLINE),
        "location" to countQuery(TABLE_LOCATION),
        "log" to countQuery(TABLE_LOG)
    )

    fun getLogs(limit: Int): List<Log> =
        jdbcTemplate.query("SELECT * FROM $TABLE_LOG ORDER BY `timestamp` DESC LIMIT $limit") { resultSet, _ -> Log(resultSet) }

    fun deleteLogs(): Int = jdbcTemplate.update("DELETE FROM $TABLE_LOG", EmptySqlParameterSource.INSTANCE)

    fun getVisits(limit: Int): List<Visit> =
        jdbcTemplate.query("SELECT * FROM $TABLE_TRACK ORDER BY visits DESC LIMIT $limit") { resultSet, _ -> Visit(resultSet) }

    fun visit(ip: String, host: String?, referrer: String?, agent: String?, page: String?) =
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

    fun getNotes(): List<Note> =
        jdbcTemplate.query("SELECT * FROM $TABLE_NOTE ORDER BY updated DESC") { resultSet, _ -> Note(resultSet) }

    fun saveNote(id: Int?, title: String, content: String?) =
        jdbcTemplate.update(
            "INSERT INTO $TABLE_NOTE (id, title, content) VALUES (:id, :title, :content) " +
                "ON DUPLICATE KEY UPDATE title = :title, content = :content",
            mapOf("id" to id, "title" to title, "content" to content)
        )

    fun deleteNote(id: Int) = jdbcTemplate.update("DELETE FROM $TABLE_NOTE WHERE id = :id", mapOf("id" to id))

    private fun countQuery(table: String): Int? =
        jdbcTemplate.queryForObject("SELECT COUNT(id) FROM $table", EmptySqlParameterSource.INSTANCE, Int::class.java)
}
