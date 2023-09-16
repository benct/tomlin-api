package no.tomlin.api.media.entity

import org.springframework.jdbc.core.RowMapper

data class Stats(
    var years: List<YearStat>,
    var ratings: List<RatingStat>,
    val total: Int,
    val seen: Int,
    val favourite: Int,
    val rating: Double,
    val episodes: Int? = null,
    val episodesSeen: Int? = null,
) {
    init {
        years = years.sortedBy { it.year }
        ratings = ratings.sortAndFill()
    }

    data class YearStat(val year: Int, val count: Int) {
        companion object {
            val rowMapper = RowMapper<YearStat> { rs, _ ->
                YearStat("${rs.getString("year")}0".toInt(), rs.getInt("count"))
            }
        }
    }

    data class RatingStat(val score: Int, val count: Int) {
        companion object {
            val rowMapper = RowMapper<RatingStat> { rs, _ ->
                RatingStat(rs.getInt("score"), rs.getInt("count"))
            }
        }
    }

    private companion object {
        fun List<RatingStat>.sortAndFill() =
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).map { score ->
                RatingStat(score, this.find { it.score == score }?.count ?: 0)
            }
    }
}
