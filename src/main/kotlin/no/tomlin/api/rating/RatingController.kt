package no.tomlin.api.rating

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.RATING
import no.tomlin.api.rating.entity.RatingItem
import no.tomlin.api.rating.entity.RatingScore
import no.tomlin.api.rating.entity.RatingSurvey
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rating")
class RatingController(private val ratingDao: RatingDao) {

    @GetMapping
    fun active(): Map<String, Any?> =
        ratingDao.getActive()?.let {
            val current = ratingDao.getActiveItems().getOrNull(it.step - 1)
            mapOf(
                "title" to it.title,
                "blind" to it.blind,
                "step" to it.step,
                "cat1" to it.cat1,
                "cat2" to it.cat2,
                "cat3" to it.cat3,
                "cat4" to it.cat4,
                "item" to current?.let { item ->
                    mapOf(
                        "id" to item.id,
                        "title" to if (it.blind) "Item ${it.step}" else item.title,
                        "subtitle" to item.subtitle,
                    )
                }
            )
        }.orEmpty()

    @PostMapping("/score")
    fun addRating(
        @RequestParam id: Long,
        @RequestParam user: String,
        @RequestParam cat1: Int,
        @RequestParam cat2: Int?,
        @RequestParam cat3: Int?,
        @RequestParam cat4: Int?,
    ): Boolean =
        ratingDao.saveScore(RatingScore(id, user, cat1, cat2, cat3, cat4))

    @Secured(ADMIN, RATING)
    @PostMapping("/next")
    fun nextStep(): Boolean = ratingDao.nextStep()

    @Secured(ADMIN, RATING)
    @PostMapping("/prev")
    fun prevStep(): Boolean = ratingDao.prevStep()

    @Secured(ADMIN, RATING)
    @GetMapping("/all")
    fun list(): List<RatingSurvey> = ratingDao.getAll()

    @Secured(ADMIN, RATING)
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): RatingSurvey? = ratingDao.get(id)

    @Secured(ADMIN, RATING)
    @GetMapping("/items/{id}")
    fun items(@PathVariable id: Long): List<RatingItem> = ratingDao.getItems(id)

    @Secured(ADMIN, RATING)
    @GetMapping("/result/{id}")
    fun getResults(@PathVariable id: Long): Any? = ratingDao.results(id)

    @Secured(ADMIN, RATING)
    @PostMapping
    fun save(
        @RequestParam id: Long?,
        @RequestParam title: String,
        @RequestParam active: Boolean?,
        @RequestParam blind: Boolean?,
        @RequestParam step: Int?,
        @RequestParam cat1: String,
        @RequestParam cat2: String?,
        @RequestParam cat3: String?,
        @RequestParam cat4: String?,
    ): Boolean =
        ratingDao.save(RatingSurvey(id, title, active ?: true, blind ?: false, step ?: 0, cat1, cat2, cat3, cat4))

    @Secured(ADMIN, RATING)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = ratingDao.delete(id)

    @Secured(ADMIN, RATING)
    @PostMapping("/item")
    fun saveItem(
        @RequestParam id: Long?,
        @RequestParam ratingId: Long,
        @RequestParam title: String,
        @RequestParam subtitle: String?
    ): Boolean =
        ratingDao.saveItem(RatingItem(id, ratingId, title, subtitle))

    @Secured(ADMIN, RATING)
    @DeleteMapping("/item/{id}")
    fun deleteItem(@PathVariable id: Long): Boolean = ratingDao.deleteItem(id)
}