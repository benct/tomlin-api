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
}
