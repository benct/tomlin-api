package no.tomlin.api.iata.entity

import no.tomlin.api.common.Extensions.required
import org.springframework.jdbc.core.RowMapper
import java.time.LocalDate
import kotlin.reflect.full.primaryConstructor

data class Airline(
    val id: String,
    val iataCode: String,
    val icaoCode: String?,
    val name: String,
    val alias: String?,
    val type: String?,
    val started: String?,
    val ended: String?,
    val operational: Boolean,
    val wiki: String?
) {
    val typeName: String = parseType(type, id)
    val isAirline: Boolean = id.startsWith("air")

    constructor(csvLine: List<String?>) : this(
        id = csvLine[0].required("id"),
        iataCode = csvLine[5] ?: "??",
        icaoCode = csvLine[4],
        name = csvLine[7].required("name"),
        alias = csvLine[8],
        type = csvLine[11],
        started = csvLine[2],
        ended = csvLine[3],
        operational = operational(csvLine[3]),
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
        "operational" to operational,
        "wiki" to wiki
    )

    companion object {
        val keys: List<String> = Airline::class.primaryConstructor?.parameters?.mapNotNull { it.name }!!

        private fun parseType(code: String?, id: String): String =
            when {
                id.startsWith("air") -> "Airline" + code?.let { " ($it)" }.orEmpty()
                id.startsWith("alc") -> "Alliance"
                id.startsWith("tec") -> "Technology"
                id.startsWith("bus") -> "Bus"
                code == "G" -> "GDS"
                code == "R" -> "Rail"
                else -> "Unknown" + code?.let { " ($it)" }.orEmpty()
            }

        private fun operational(date: String?): Boolean =
            date.isNullOrBlank() || LocalDate.parse(date).isAfter(LocalDate.now())

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
                rs.getBoolean("operational"),
                rs.getString("wiki")
            )
        }
    }
}
