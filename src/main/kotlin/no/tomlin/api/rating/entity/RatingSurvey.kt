package no.tomlin.api.rating.entity

import org.springframework.jdbc.core.RowMapper
import java.time.LocalDateTime

data class RatingSurvey(
    val id: Long?,
    val title: String,
    val active: Boolean,
    val blind: Boolean,
    val step: Int,
    val cat1: String,
    val cat2: String?,
    val cat3: String?,
    val cat4: String?,
    val updated: LocalDateTime? = null,
) {

    fun asDaoMap() = mapOf(
        "id" to id,
        "title" to title,
        "active" to active,
        "blind" to blind,
        "step" to step,
        "cat1" to cat1,
        "cat2" to cat2,
        "cat3" to cat3,
        "cat4" to cat4,
    )

    companion object {
        val rowMapper = RowMapper<RatingSurvey> { rs, _ ->
            RatingSurvey(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getBoolean("active"),
                rs.getBoolean("blind"),
                rs.getInt("step"),
                rs.getString("cat1"),
                rs.getString("cat2"),
                rs.getString("cat3"),
                rs.getString("cat4"),
                rs.getTimestamp("updated").toLocalDateTime(),
            )
        }
    }
}
