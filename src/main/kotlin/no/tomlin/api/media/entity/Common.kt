package no.tomlin.api.media.entity

data class Genre(
    val id: Long,
    val name: String
)

data class ProductionCompany(
    val id: Long,
    val name: String,
    val logoPath: String? = null
)

data class Person(
    val id: Long,
    val name: String,
    val profilePath: String? = null
)

data class Network(
    val id: Long,
    val name: String,
    val logoPath: String? = null,
    val originCountry: String? = null
)

data class ExternalIds(
    val imdbID: String? = null,
    val facebookID: String? = null,
    val instagramID: String? = null,
    val twitterID: String? = null
)
