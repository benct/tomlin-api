package no.tomlin.api.rating.entity

import no.tomlin.api.common.Constants.TABLE_RATING
import java.sql.ResultSet
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
    constructor(resultSet: ResultSet) : this(
        resultSet.getLong("id"),
        resultSet.getString("title"),
        resultSet.getBoolean("active"),
        resultSet.getBoolean("blind"),
        resultSet.getInt("step"),
        resultSet.getString("cat1"),
        resultSet.getString("cat2"),
        resultSet.getString("cat3"),
        resultSet.getString("cat4"),
        resultSet.getTimestamp("updated").toLocalDateTime(),
    )

    private val keys = asDaoMap().keys

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

    fun insertStatement(): String =
        "INSERT INTO $TABLE_RATING (${keys.joinToString { "`${it}`" }}) VALUES (${keys.joinToString { ":$it" }}) " +
            "ON DUPLICATE KEY UPDATE ${keys.joinToString { "`${it}` = :${it}" }}"
}
