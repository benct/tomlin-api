package no.tomlin.api.common

object Constants {
    // User roles
    const val USER = "ROLE_USER" // default user role
    const val ADMIN = "ROLE_ADMIN" // administration
    const val PRIVATE = "ROLE_PRIVATE" // notes, files, flights, finn
    const val MEDIA = "ROLE_MEDIA" // watchlist, movies, tv
    const val RATING = "ROLE_RATING"
    const val QRATOR = "ROLE_QRATOR"

    val ALL_ROLES = setOf(USER, ADMIN, PRIVATE, MEDIA, RATING, QRATOR)

    // Pagination
    const val PAGE_SIZE = 50

    // Database tables
    private const val TABLE_PREFIX = "tomlin_"
    const val TABLE_SETTINGS = "${TABLE_PREFIX}settings"
    const val TABLE_USER = "${TABLE_PREFIX}user"
    const val TABLE_ROLE = "${TABLE_PREFIX}role"
    const val TABLE_LOG = "${TABLE_PREFIX}log"
    const val TABLE_TRACK = "${TABLE_PREFIX}track"
    const val TABLE_NOTE = "${TABLE_PREFIX}note"
    const val TABLE_FLIGHT = "${TABLE_PREFIX}flight"
    const val TABLE_BEEN = "${TABLE_PREFIX}been"
    const val TABLE_FINN = "${TABLE_PREFIX}finn"
    const val TABLE_QRATOR = "${TABLE_PREFIX}qrator"
    const val TABLE_RATING = "${TABLE_PREFIX}rating"
    const val TABLE_RATING_ITEM = "${TABLE_PREFIX}rating_item"
    const val TABLE_RATING_SCORE = "${TABLE_PREFIX}rating_score"
    const val TABLE_MOVIE = "${TABLE_PREFIX}movie"
    const val TABLE_TV = "${TABLE_PREFIX}tv"
    const val TABLE_SEASON = "${TABLE_PREFIX}season"
    const val TABLE_EPISODE = "${TABLE_PREFIX}episode"
    const val TABLE_AIRLINE = "${TABLE_PREFIX}iata_airline"
    const val TABLE_LOCATION = "${TABLE_PREFIX}iata_location"
}
