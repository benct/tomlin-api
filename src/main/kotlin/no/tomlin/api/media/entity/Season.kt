package no.tomlin.api.media.entity

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import no.tomlin.api.common.Constants.TABLE_SEASON
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.common.JsonUtils.parseJson

data class Season(
    val id: Long,
    val name: String,
    val airDate: String,
    val overview: String,
    val posterPath: String? = null,
    val seasonNumber: Long,
    val episodes: List<Episode>
) : Media() {

    override val table = TABLE_SEASON

    override val keys = toDaoMap(-1).keys

    fun toDaoMap(tvId: Long) = mapOf(
        "id" to id,
        "tv_id" to tvId,
        "season" to seasonNumber,
        "title" to name,
        "poster" to posterPath,
        "release_date" to airDate.nullIfBlank(),
        "overview" to overview.nullIfBlank()
    )

    companion object {
        fun String.parseSeason() = this.parseJson<Season>(SNAKE_CASE)
    }
}
