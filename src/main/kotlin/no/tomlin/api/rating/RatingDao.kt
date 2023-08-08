package no.tomlin.api.rating

import no.tomlin.api.common.Constants.TABLE_RATING
import no.tomlin.api.common.Constants.TABLE_RATING_ITEM
import no.tomlin.api.common.Constants.TABLE_RATING_SCORE
import no.tomlin.api.common.Extensions.checkRowsAffected
import no.tomlin.api.rating.entity.RatingItem
import no.tomlin.api.rating.entity.RatingScore
import no.tomlin.api.rating.entity.RatingSurvey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class RatingDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Cacheable("active")
    fun getActive(): RatingSurvey? = jdbcTemplate.queryForObject(
        "SELECT * FROM $TABLE_RATING WHERE `active` = 1 ORDER BY `id` DESC LIMIT 1",
        EmptySqlParameterSource.INSTANCE
    ) { resultSet, _ -> RatingSurvey(resultSet) }

    @Cacheable("activeItems")
    fun getActiveItems(): List<RatingItem> = jdbcTemplate.query(
        "SELECT i.* FROM $TABLE_RATING_ITEM i JOIN $TABLE_RATING r ON r.id = i.rating_id " +
            "WHERE r.active = 1 ORDER BY i.id ASC",
        EmptySqlParameterSource.INSTANCE
    ) { resultSet, _ -> RatingItem(resultSet) }

    @CacheEvict("active")
    fun nextStep(): Boolean = jdbcTemplate.update(
        "UPDATE $TABLE_RATING SET `step` = `step` + 1 WHERE `active` = 1",
        EmptySqlParameterSource.INSTANCE
    ).checkRowsAffected()

    @CacheEvict("active")
    fun prevStep(): Boolean = jdbcTemplate.update(
        "UPDATE $TABLE_RATING SET `step` = `step` - 1 WHERE `active` = 1",
        EmptySqlParameterSource.INSTANCE
    ).checkRowsAffected()

    fun getAll(): List<RatingSurvey> = jdbcTemplate.query(
        "SELECT * FROM $TABLE_RATING ORDER BY `id` ASC",
        EmptySqlParameterSource.INSTANCE
    ) { resultSet, _ -> RatingSurvey(resultSet) }

    fun get(id: Long): RatingSurvey? = jdbcTemplate.queryForObject(
        "SELECT * FROM $TABLE_RATING WHERE `id` = :id",
        mapOf("id" to id)
    ) { resultSet, _ -> RatingSurvey(resultSet) }

    fun getItems(id: Long): List<RatingItem> = jdbcTemplate.query(
        "SELECT * FROM $TABLE_RATING_ITEM WHERE `rating_id` = :id ORDER BY `id` ASC",
        mapOf("id" to id),
    ) { resultSet, _ -> RatingItem(resultSet) }

    fun results(id: Long): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        "SELECT i.id, i.title, i.subtitle, COUNT(DISTINCT s.user_id) as answers, " +
            "AVG(s.cat1) as avg1, AVG(s.cat2) as avg2, AVG(s.cat3) as avg3, AVG(s.cat4) as avg4, " +
            "SUM(s.cat1) as sum1, SUM(s.cat2) as sum2, SUM(s.cat3) as sum3, SUM(s.cat4) as sum4, " +
            "SUM(s.cat1 + IFNULL(s.cat2, 0) + IFNULL(s.cat3, 0) + IFNULL(s.cat4, 0)) as total " +
            "FROM $TABLE_RATING_ITEM i JOIN $TABLE_RATING_SCORE s ON i.id = s.item_id " +
            "WHERE i.rating_id = :id GROUP BY i.id ORDER BY i.id ASC",
        mapOf("id" to id),
    )

    fun saveScore(score: RatingScore): Boolean = jdbcTemplate
        .update(score.insertStatement(), score.asDaoMap())
        .checkRowsAffected()

    @CacheEvict(value = ["active", "activeItems"], allEntries = true)
    fun save(rating: RatingSurvey): Boolean = jdbcTemplate
        .update(rating.insertStatement(), rating.asDaoMap())
        .checkRowsAffected()

    @CacheEvict(value = ["active", "activeItems"], allEntries = true)
    fun delete(id: Long): Boolean = jdbcTemplate
        .update("DELETE FROM $TABLE_RATING WHERE id = :id", mapOf("id" to id)) > 0

    @CacheEvict("activeItems", allEntries = true)
    fun saveItem(rating: RatingItem): Boolean = jdbcTemplate
        .update(rating.insertStatement(), rating.asDaoMap())
        .checkRowsAffected()

    @CacheEvict("activeItems", allEntries = true)
    fun deleteItem(id: Long): Boolean = jdbcTemplate
        .update("DELETE FROM $TABLE_RATING_ITEM WHERE id = :id", mapOf("id" to id)) > 0
}
