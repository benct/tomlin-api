package no.tomlin.api.media.entity

import no.tomlin.api.common.Constants.TABLE_EPISODE
import no.tomlin.api.common.Extensions.nullIfBlank

data class Episode(
    val id: Long,
    val showId: Long,
    val name: String,
    val overview: String,
    val airDate: String? = null,
    val productionCode: String,
    val stillPath: String? = null,
    val seasonNumber: Long,
    val episodeNumber: Long,
    val voteAverage: Double,
    val voteCount: Long
) : Media() {

    override val table = TABLE_EPISODE

    override val keys = toDaoMap(-1).keys

    fun toDaoMap(seasonId: Long) = mapOf(
        "id" to id,
        "season_id" to seasonId,
        "tv_id" to showId,
        "title" to name,
        "poster" to stillPath,
        "season" to seasonNumber,
        "episode" to episodeNumber,
        "release_date" to airDate.nullIfBlank(),
        "overview" to overview.nullIfBlank(),
        "production_code" to productionCode.nullIfBlank(),
        "rating" to voteAverage,
        "votes" to voteCount
    )
}
