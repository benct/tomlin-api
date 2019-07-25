package no.tomlin.api.iata.entity

import no.tomlin.api.common.Extensions.required
import java.sql.ResultSet
import java.time.LocalDate

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
    val type: String,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val operational: Boolean,
    val wiki: String?
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getString("id"),
        resultSet.getString("iataCode"),
        resultSet.getString("icaoCode"),
        resultSet.getString("cityCode"),
        resultSet.getString("cityName"),
        resultSet.getString("name"),
        resultSet.getString("area"),
        resultSet.getString("areaCode"),
        resultSet.getString("country"),
        resultSet.getString("countryCode"),
        resultSet.getString("continent"),
        resultSet.getString("type"),
        resultSet.getDouble("latitude"),
        resultSet.getDouble("longitude"),
        resultSet.getString("timezone"),
        resultSet.getBoolean("operational"),
        resultSet.getString("wiki")
    )

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
        type = type(csvLine[41]),
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
        private fun type(code: String?): String =
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
    }
}
