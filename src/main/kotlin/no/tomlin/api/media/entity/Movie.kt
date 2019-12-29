package no.tomlin.api.media.entity

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import no.tomlin.api.common.Constants.TABLE_MOVIE
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.common.JsonUtils.parseJson
import java.util.*

data class Movie(
    val id: Long,
    val imdbId: String? = null, // blank
    val title: String,
    val originalTitle: String,
    val originalLanguage: String,
    val posterPath: String? = null,
    val runtime: Long? = null,
    val releaseDate: String,
    val overview: String, // blank
    val tagline: String, // blank
    val budget: Long,
    val revenue: Long,
    val voteAverage: Double,
    val voteCount: Long,
    val genres: List<Genre>,
    val productionCompanies: List<ProductionCompany>
) {
    private val keys = toDaoMap().keys

    val insertStatement: String =
        "INSERT INTO $TABLE_MOVIE (${keys.joinToString { "`${it}`" }}) VALUES (${keys.joinToString { ":$it" }}) " +
            "ON DUPLICATE KEY UPDATE ${keys.joinToString { "`${it}` = :${it}" }}"

    val updateStatement: String = "UPDATE $TABLE_MOVIE SET ${keys.joinToString { "`${it}` = :${it}" }} WHERE `id` = :id"

    fun toDaoMap() = mapOf(
        "id" to id,
        "imdb_id" to imdbId.nullIfBlank(),
        "title" to title,
        "original_title" to if (title != originalTitle) originalTitle else null,
        "genres" to genres.joinToString { it.name }.nullIfBlank(),
        "language" to Locale(originalLanguage).displayLanguage,
        "poster" to posterPath,
        "runtime" to (runtime ?: 0),
        "release_date" to releaseDate,
        "release_year" to releaseDate.subSequence(0, 4),
        "overview" to overview.nullIfBlank(),
        "tagline" to tagline.nullIfBlank(),
        "budget" to budget,
        "revenue" to revenue,
        "rating" to voteAverage,
        "votes" to voteCount
    )

    companion object {
        fun String.parseMovie() = this.parseJson<Movie>(SNAKE_CASE)
    }
}

data class Genre(
    val id: Long,
    val name: String
)

data class ProductionCompany(
    val id: Long,
    val name: String,
    val logoPath: String? = null
)
