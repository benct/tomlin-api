package no.tomlin.api.iata.entity

import no.tomlin.api.common.Extensions.required
import org.springframework.jdbc.core.RowMapper

data class Airline(
    val id: String,
    val iataCode: String,
    val icaoCode: String?,
    val name: String,
    val alias: String?,
    val type: String,
    val started: String?,
    val ended: String?,
    val wiki: String?
) {

    constructor(csvLine: List<String?>) : this(
        id = csvLine[0].required("id"),
        iataCode = csvLine[5] ?: "??",
        icaoCode = csvLine[4],
        name = csvLine[7].required("name"),
        alias = csvLine[8],
        type = type(csvLine[11]),
        started = csvLine[2],
        ended = csvLine[3],
        wiki = csvLine[12]
    )

    fun asDaoMap() = mapOf(
        "id" to id,
        "iataCode" to iataCode,
        "icaoCode" to icaoCode,
        "name" to name,
        "alias" to alias,
        "type" to type,
        "started" to started,
        "ended" to ended,
        "wiki" to wiki
    )

    companion object {
        private fun type(code: String?): String =
            when (code) {
                null -> "Airline"
                "P", "T" -> "Airline ($code)"
                "R" -> "Railway (R)"
                "C" -> "Cargo (C)"
                "G" -> "GDS (G)"
                else -> "Unknown ($code)"
            }

        val rowMapper = RowMapper<Airline> { rs, _ ->
            Airline(
                rs.getString("id"),
                rs.getString("iataCode"),
                rs.getString("icaoCode"),
                rs.getString("name"),
                rs.getString("alias"),
                rs.getString("type"),
                rs.getString("started"),
                rs.getString("ended"),
                rs.getString("wiki")
            )
        }
    }
}
