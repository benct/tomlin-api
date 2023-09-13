package no.tomlin.api.media.entity

import java.sql.ResultSet

data class Stats(
    val years: List<YearStat>,
    val ratings: List<RatingStat>,
    val total: Int,
    val seen: Int,
    val favourite: Int,
    val rating: Double,
    val episodes: Int? = null,
    val episodesSeen: Int? = null,
) {
    constructor(
        years: List<YearStat>,
        ratings: List<RatingStat>,
        resultSet: ResultSet,
        hasEpisodes: Boolean = false
    ) : this(
        years,
        ratings.sortAndFill(),
        resultSet.getInt("total"),
        resultSet.getInt("seen"),
        resultSet.getInt("favourite"),
        resultSet.getDouble("rating"),
        if (hasEpisodes) resultSet.getInt("episodes") else null,
        if (hasEpisodes) resultSet.getInt("seen_episodes") else null,
    )

    data class YearStat(val year: Int, val count: Int) {
        constructor(resultSet: ResultSet) : this(
            "${resultSet.getString("year")}0".toInt(),
            resultSet.getInt("count")
        )
    }

    data class RatingStat(val score: Int, val count: Int) {
        constructor(resultSet: ResultSet) : this(
            resultSet.getInt("score"),
            resultSet.getInt("count")
        )
    }

    private companion object {
        fun List<RatingStat>.sortAndFill() =
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).map { score ->
                RatingStat(score, this.find { it.score == score }?.count ?: 0)
            }
    }
}
