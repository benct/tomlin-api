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
        Select(TABLE_RATING)
            .where("active").eq(true)
            .orderBy("id" to "DESC"),
        RatingSurvey.rowMapper,
    )

    @Cacheable("activeItems")
    fun getActiveItems(): List<RatingItem> = jdbc.query(
        Select(TABLE_RATING_ITEM)
            .join(TABLE_RATING).on("id", "rating_id")
            .where("active").eq(true)
            .orderBy("id"),
        RatingItem.rowMapper,
    )

    @CacheEvict("active")
    fun nextStep(): Boolean = jdbc.update(
        Increment(TABLE_RATING)
            .column("step")
            .where("active").eq(true)
    )

    @CacheEvict("active")
    fun prevStep(): Boolean = jdbc.update(
        Decrement(TABLE_RATING)
            .column("step")
            .where("active").eq(true)
    )

    fun getAll(): List<RatingSurvey> = jdbc.query(
        Select(TABLE_RATING).orderBy("id"),
        RatingSurvey.rowMapper,
    )

    fun get(id: Long): RatingSurvey? = jdbc.queryForObject(
        Select(TABLE_RATING).where("id").eq(id),
        RatingSurvey.rowMapper
    )

    fun getItems(id: Long): List<RatingItem> = jdbc.query(
        Select(TABLE_RATING_ITEM)
            .where("rating_id").eq(id)
            .orderBy("id"),
        RatingItem.rowMapper
    )

    fun results(id: Long): List<Map<String, Any?>> = jdbc.queryForList(
        Select(TABLE_RATING_ITEM)
            .columns("id", "title", "subtitle")
            .column(TABLE_RATING_SCORE, "user_id").countDistinct("answers")
            .column(TABLE_RATING_SCORE, "cat1").avg("avg1")
            .column(TABLE_RATING_SCORE, "cat2").avg("avg2")
            .column(TABLE_RATING_SCORE, "cat3").avg("avg3")
            .column(TABLE_RATING_SCORE, "cat4").avg("avg4")
            .column(TABLE_RATING_SCORE, "cat1").sum("sum1")
            .column(TABLE_RATING_SCORE, "cat2").sum("sum2")
            .column(TABLE_RATING_SCORE, "cat3").sum("sum3")
            .column(TABLE_RATING_SCORE, "cat4").sum("sum4")
            .column("SUM(cat1 + IFNULL(cat2, 0) + IFNULL(cat3, 0) + IFNULL(cat4, 0))").custom("total")
            .join(TABLE_RATING_SCORE).on("item_id", "id")
            .where("rating_id").eq(id)
            .groupBy("id")
            .orderBy("id")
    )

    fun saveScore(score: RatingScore): Boolean = jdbc.update(
        Upsert(TABLE_RATING_SCORE).data(score.asDaoMap())
    )

    @CacheEvict(value = ["active", "activeItems"], allEntries = true)
    fun save(rating: RatingSurvey): Boolean = jdbc.update(
        Upsert(TABLE_RATING).data(rating.asDaoMap())
    )

    @CacheEvict(value = ["active", "activeItems"], allEntries = true)
    fun delete(id: Long): Boolean = jdbc.update(
        Delete(TABLE_RATING).where("id").eq(id)
    )

    @CacheEvict("activeItems", allEntries = true)
    fun saveItem(ratingItem: RatingItem): Boolean = jdbc.update(
        Upsert(TABLE_RATING_ITEM).data(ratingItem.asDaoMap())
    )

    @CacheEvict("activeItems", allEntries = true)
    fun deleteItem(id: Long): Boolean = jdbc.update(
        Delete(TABLE_RATING_ITEM).where("id").eq(id)
    )
}
