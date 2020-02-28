package no.tomlin.api.media.entity

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import no.tomlin.api.common.Constants.TABLE_TV
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.common.JsonUtils.parseJson
import java.util.*

data class TV(
    val id: Long,
    val name: String,
    val originalName: String,
    val originalLanguage: String,
    val posterPath: String? = null,
    val episodeRunTime: List<Int>,
    val inProduction: Boolean,
    val firstAirDate: String? = null,
    val lastAirDate: String? = null,
    val overview: String,
    val status: String,
    val type: String,
    val voteAverage: Double,
    val voteCount: Long,
    val numberOfEpisodes: Long,
    val numberOfSeasons: Long,
    val seasons: List<SimpleSeason>,
    val genres: List<Genre>,
    val networks: List<Network>,
    val productionCompanies: List<ProductionCompany>,
    val createdBy: List<Person>,
    val externalIds: ExternalIds
) : Media() {

    override val table = TABLE_TV

    override val keys = toDaoMap().keys

    fun toDaoMap() = mapOf(
        "id" to id,
        "imdb_id" to externalIds.imdbID.nullIfBlank(),
        "facebook_id" to externalIds.facebookID.nullIfBlank(),
        "instagram_id" to externalIds.instagramID.nullIfBlank(),
        "twitter_id" to externalIds.twitterID.nullIfBlank(),
        "title" to name,
        "original_title" to if (name != originalName) originalName else null,
        "genres" to genres.joinToString { it.name }.nullIfBlank(),
        "language" to Locale(originalLanguage).displayLanguage,
        "poster" to posterPath,
        "runtime" to episodeRunTime.joinToString { it.toString() }.nullIfBlank(),
        "release_date" to firstAirDate.nullIfBlank(),
        "release_year" to firstAirDate.nullIfBlank()?.substring(0, 4),
        "end_year" to if (!inProduction && !lastAirDate.isNullOrBlank()) lastAirDate.substring(0, 4) else null,
        "overview" to overview.nullIfBlank(),
        "status" to status.nullIfBlank(),
        "series_type" to type.nullIfBlank(),
        "networks" to networks.joinToString { it.name }.nullIfBlank(),
        "production_companies" to productionCompanies.joinToString { it.name }.nullIfBlank(),
        "created_by" to createdBy.joinToString { it.name }.nullIfBlank(),
        "number_of_episodes" to numberOfEpisodes,
        "number_of_seasons" to numberOfSeasons,
        "rating" to voteAverage,
        "votes" to voteCount
    )

    companion object {
        fun String.parseTV() = this.parseJson<TV>(SNAKE_CASE)
    }

    data class SimpleSeason(
        val id: Long,
        val name: String,
        val overview: String? = null, // blank
        val airDate: String? = null,
        val posterPath: String? = null,
        val episodeCount: Long,
        val seasonNumber: Long
    )
}
