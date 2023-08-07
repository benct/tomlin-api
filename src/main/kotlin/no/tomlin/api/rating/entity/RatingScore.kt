package no.tomlin.api.rating.entity

import no.tomlin.api.common.Constants.TABLE_RATING_SCORE
import java.sql.ResultSet

data class RatingScore(
    val itemId: Long,
    val userId: String,
    val cat1: Int,
    val cat2: Int? = null,
    val cat3: Int? = null,
    val cat4: Int? = null,
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getLong("item_id"),
        resultSet.getString("user_id"),
        resultSet.getInt("cat1"),
        resultSet.getInt("cat2"),
        resultSet.getInt("cat3"),
        resultSet.getInt("cat4"),
    )

    private val keys = asDaoMap().keys

    fun asDaoMap() = mapOf(
        "item_id" to itemId,
        "user_id" to userId,
        "cat1" to cat1,
        "cat2" to cat2,
        "cat3" to cat3,
        "cat4" to cat4,
    )

    fun insertStatement(): String =
        "INSERT INTO $TABLE_RATING_SCORE (${keys.joinToString { "`${it}`" }}) VALUES (${keys.joinToString { ":$it" }}) " +
            "ON DUPLICATE KEY UPDATE ${keys.joinToString { "`${it}` = :${it}" }}"
}
