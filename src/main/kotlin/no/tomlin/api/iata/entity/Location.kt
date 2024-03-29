package no.tomlin.api.iata.entity

import no.tomlin.api.common.Extensions.required
import org.springframework.jdbc.core.RowMapper
import java.time.LocalDate
import kotlin.reflect.full.primaryConstructor

data class Location(
    val id: String,
    val iataCode: String,
    val icaoCode: String?,
    val cityCode: String?,
    val cityName: String?,
    val name: String,
    val area: String?,
    val areaCode: String?,
    val country: String,
    val countryCode: String,
    val continent: String,
    val type: String?,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val operational: Boolean,
    val wiki: String?
) {
    val typeName: String = parseType(type)
    val isAirport: Boolean = type?.let { it == "A" || it == "CA" || it == "C" } ?: false

    constructor(csvLine: List<String?>) : this(
        id = "${csvLine[0]}_${csvLine[41]}_${csvLine[4]}",
        iataCode = csvLine[0] ?: "???",
        icaoCode = csvLine[1],
        cityCode = csvLine[36]?.take(3),
        cityName = csvLine[37],
        name = csvLine[6].required("name"),
        area = csvLine[21],
        areaCode = csvLine[20],
        country = csvLine[18].required("country"),
        countryCode = csvLine[16].required("countryCode"),
        continent = csvLine[19].required("continent"),
        type = csvLine[41],
        latitude = csvLine[8]?.toDoubleOrNull(),
        longitude = csvLine[9]?.toDoubleOrNull(),
        timezone = csvLine[31],
        operational = operational(csvLine[14]),
        wiki = csvLine[42]
    )

    fun asDaoMap() = mapOf(
        "id" to id,
        "iataCode" to iataCode,
        "icaoCode" to icaoCode,
        "cityCode" to cityCode,
        "cityName" to cityName,
        "name" to name,
        "area" to area,
        "areaCode" to areaCode,
        "country" to country,
        "countryCode" to countryCode,
        "continent" to continent,
        "type" to type,
        "latitude" to latitude,
        "longitude" to longitude,
        "timezone" to timezone,
        "operational" to operational,
        "wiki" to wiki
    )

    companion object {
        val keys: List<String> = Location::class.primaryConstructor?.parameters?.mapNotNull { it.name }!!

        private fun parseType(code: String?): String =
            when (code) {
                "C" -> "Metropolitan Area (C)"
                "A", "CA" -> "Airport ($code)"
                "B", "CB" -> "Bus Station ($code)"
                "R", "CR" -> "Railway Station ($code)"
                "H", "CH" -> "Heliport ($code)"
                "P", "CP" -> "Ferry Port ($code)"
                "O" -> "Off-line Point (O)"
                else -> "Unsupported ($code)"
            }

        private fun operational(date: String?): Boolean =
            date.isNullOrBlank() || LocalDate.parse(date).isAfter(LocalDate.now())

        val rowMapper = RowMapper<Location> { rs, _ ->
            Location(
                rs.getString("id"),
                rs.getString("iataCode"),
                rs.getString("icaoCode"),
                rs.getString("cityCode"),
                rs.getString("cityName"),
                rs.getString("name"),
                rs.getString("area"),
                rs.getString("areaCode"),
                rs.getString("country"),
                rs.getString("countryCode"),
                rs.getString("continent"),
                rs.getString("type"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("timezone"),
                rs.getBoolean("operational"),
                rs.getString("wiki")
            )
        }
    }
}
