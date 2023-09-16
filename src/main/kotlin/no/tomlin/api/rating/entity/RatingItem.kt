package no.tomlin.api.rating.entity

import org.springframework.jdbc.core.RowMapper

data class RatingItem(
    val id: Long?,
    val ratingId: Long,
    val title: String,
    val subtitle: String? = null,
) {

    fun asDaoMap() = mapOf(
        "id" to id,
        "rating_id" to ratingId,
        "title" to title,
        "subtitle" to subtitle,
    )

    companion object {
        val rowMapper = RowMapper<RatingItem> { rs, _ ->
            RatingItem(
                rs.getLong("id"),
                rs.getLong("rating_id"),
                rs.getString("title"),
                rs.getString("subtitle"),
            )
        }
    }
}
