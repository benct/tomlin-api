package no.tomlin.api.rating.entity

import org.springframework.jdbc.core.RowMapper

data class RatingScore(
    val itemId: Long,
    val userId: String,
    val cat1: Int,
    val cat2: Int? = null,
    val cat3: Int? = null,
    val cat4: Int? = null,
) {

    fun asDaoMap() = mapOf(
        "item_id" to itemId,
        "user_id" to userId,
        "cat1" to cat1,
        "cat2" to cat2,
        "cat3" to cat3,
        "cat4" to cat4,
    )

    companion object {
        val rowMapper = RowMapper<RatingScore> { rs, _ ->
            RatingScore(
                rs.getLong("item_id"),
                rs.getString("user_id"),
                rs.getInt("cat1"),
                rs.getInt("cat2"),
                rs.getInt("cat3"),
                rs.getInt("cat4"),
            )
        }
    }
}
