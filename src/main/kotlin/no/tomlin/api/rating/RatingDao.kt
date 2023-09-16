package no.tomlin.api.rating

import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForList
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.*
import no.tomlin.api.rating.entity.RatingItem
import no.tomlin.api.rating.entity.RatingScore
import no.tomlin.api.rating.entity.RatingSurvey
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class RatingDao(private val jdbc: NamedParameterJdbcTemplate) {

    @Cacheable("active")
    fun getActive(): RatingSurvey? = jdbc.queryForObject(
        Select(
            from = TABLE_RATING,
            where = Where("active" to true),
            orderBy = OrderBy("id" to "DESC"),
        ),
        RatingSurvey.rowMapper,
    )

    @Cacheable("activeItems")
    fun getActiveItems(): List<RatingItem> = jdbc.query(
        Select(
            columns = "i.*",
            from = TABLE_RATING_ITEM,
            join = Join(TABLE_RATING, "r.id" to "i.rating_id"),
            where = Where("active" to true),
            orderBy = OrderBy("i.id"),
        ),
        RatingItem.rowMapper,
    )

    @CacheEvict("active")
    fun nextStep(): Boolean = jdbc.update(
        Increment(TABLE_RATING, column = "step", Where("active" to true))
    )

    @CacheEvict("active")
    fun prevStep(): Boolean = jdbc.update(
        Decrement(TABLE_RATING, column = "step", Where("active" to true))
    )

    fun getAll(): List<RatingSurvey> = jdbc.query(
        Select(from = TABLE_RATING, orderBy = OrderBy("id")),
        RatingSurvey.rowMapper,
    )

    fun get(id: Long): RatingSurvey? = jdbc.queryForObject(
        Select(from = TABLE_RATING, where = Where("id" to id)),
        RatingSurvey.rowMapper
    )

    fun getItems(id: Long): List<RatingItem> = jdbc.query(
        Select(from = TABLE_RATING_ITEM, where = Where("rating_id" to id), orderBy = OrderBy("id")),
        RatingItem.rowMapper
    )

    fun results(id: Long): List<Map<String, Any?>> = jdbc.queryForList(
        Select(
            columns = listOf(
                "i.id", "i.title", "i.subtitle", "COUNT(DISTINCT s.user_id) AS answers",
                "AVG(s.cat1) AS avg1", "AVG(s.cat2) AS avg2", "AVG(s.cat3) AS avg3", "AVG(s.cat4) AS avg4",
                "SUM(s.cat1) AS sum1", "SUM(s.cat2) AS sum2", "SUM(s.cat3) AS sum3", "SUM(s.cat4) AS sum4",
                "SUM(s.cat1 + IFNULL(s.cat2, 0) + IFNULL(s.cat3, 0) + IFNULL(s.cat4, 0)) AS total"
            ).joinToString(),
            from = TABLE_RATING_ITEM,
            join = Join(TABLE_RATING_SCORE, "i.id" to "s.item_id"),
            where = Where("rating_id" to id),
            groupBy = GroupBy("i.id"),
            orderBy = OrderBy("i.id"),
        )
    )

    fun saveScore(score: RatingScore): Boolean = jdbc.update(
        Upsert(TABLE_RATING_SCORE, score.asDaoMap())
    )

    @CacheEvict(value = ["active", "activeItems"], allEntries = true)
    fun save(rating: RatingSurvey): Boolean = jdbc.update(
        Upsert(TABLE_RATING, rating.asDaoMap())
    )

    @CacheEvict(value = ["active", "activeItems"], allEntries = true)
    fun delete(id: Long): Boolean = jdbc.update(
        Delete(TABLE_RATING, Where("id" to id))
    )

    @CacheEvict("activeItems", allEntries = true)
    fun saveItem(ratingItem: RatingItem): Boolean = jdbc.update(
        Upsert(TABLE_RATING_ITEM, ratingItem.asDaoMap())
    )

    @CacheEvict("activeItems", allEntries = true)
    fun deleteItem(id: Long): Boolean = jdbc.update(
        Delete(TABLE_RATING_ITEM, Where("id" to id))
    )
}
