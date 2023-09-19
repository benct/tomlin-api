package no.tomlin.api.db

enum class Table(private val tableName: String) {
    TABLE_SETTINGS("settings"),
    TABLE_USER("user"),
    TABLE_ROLE("role"),
    TABLE_LOG("log"),
    TABLE_TRACK("track"),
    TABLE_NOTE("note"),
    TABLE_FLIGHT("flight"),
    TABLE_BEEN("been"),
    TABLE_FINN("finn"),
    TABLE_QRATOR("qrator"),
    TABLE_RATING("rating"),
    TABLE_RATING_ITEM("rating_item"),
    TABLE_RATING_SCORE("rating_score"),
    TABLE_MOVIE("movie"),
    TABLE_TV("tv"),
    TABLE_SEASON("season"),
    TABLE_EPISODE("episode"),
    TABLE_IATA_AIRLINE("iata_airline"),
    TABLE_IATA_LOCATION("iata_location"),
    ;

    override fun toString(): String = TABLE_PREFIX + this.tableName

    private companion object {
        const val TABLE_PREFIX = "tomlin_"
    }
}