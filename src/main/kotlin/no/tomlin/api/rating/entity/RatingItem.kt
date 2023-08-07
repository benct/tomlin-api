package no.tomlin.api.rating.entity

import no.tomlin.api.common.Constants.TABLE_RATING_ITEM
import java.sql.ResultSet

data class RatingItem(
    val id: Long?,
    val ratingId: Long,
    val title: String,
    val subtitle: String? = null,
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getLong("id"),
        resultSet.getLong("rating_id"),
        resultSet.getString("title"),
        resultSet.getString("subtitle"),
    )

    private val keys = asDaoMap().keys

    fun asDaoMap() = mapOf(
        "id" to id,
        "rating_id" to ratingId,
        "title" to title,
        "subtitle" to subtitle,
    )

    fun insertStatement(): String =
        "INSERT INTO $TABLE_RATING_ITEM (${keys.joinToString { "`${it}`" }}) VALUES (${keys.joinToString { ":$it" }}) " +
            "ON DUPLICATE KEY UPDATE ${keys.joinToString { "`${it}` = :${it}" }}"
}
