package no.tomlin.api.common

object Constants {
    // User roles
    const val USER = "ROLE_USER"
    const val ADMIN = "ROLE_ADMIN"

    // Media
    const val PAGE_SIZE = 50

    // Database tables
    private const val TABLE_PREFIX = "tomlin_"
    const val TABLE_SETTINGS = "${TABLE_PREFIX}settings"
    const val TABLE_LOG = "${TABLE_PREFIX}log"
    const val TABLE_TRACK = "${TABLE_PREFIX}track"
    const val TABLE_NOTE = "${TABLE_PREFIX}note"
    const val TABLE_FLIGHT = "${TABLE_PREFIX}flight"
    const val TABLE_FINN = "${TABLE_PREFIX}finn"
    const val TABLE_HASS = "${TABLE_PREFIX}home"
    const val TABLE_MOVIE = "${TABLE_PREFIX}movie"
    const val TABLE_TV = "${TABLE_PREFIX}tv"
    const val TABLE_SEASON = "${TABLE_PREFIX}season"
    const val TABLE_EPISODE = "${TABLE_PREFIX}episode"
    const val TABLE_AIRLINE = "${TABLE_PREFIX}iata_airline"
    const val TABLE_LOCATION = "${TABLE_PREFIX}iata_location"
}
