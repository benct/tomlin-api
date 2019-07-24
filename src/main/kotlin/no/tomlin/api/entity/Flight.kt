package no.tomlin.api.entity

import java.sql.ResultSet
import java.time.LocalDateTime

data class Flight(
    val id: Int?,
    val origin: String,
    val destination: String,
    val departure: LocalDateTime,
    val arrival: LocalDateTime,
    val carrier: String,
    val number: String,
    val cabin: String?,
    val aircraft: String?,
    val seat: String?,
    val reference: String,
    val into: String?
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getInt("id"),
        resultSet.getString("origin"),
        resultSet.getString("destination"),
        LocalDateTime.parse(resultSet.getString("departure")),
        LocalDateTime.parse(resultSet.getString("arrival")),
        resultSet.getString("carrier"),
        resultSet.getString("number"),
        resultSet.getString("cabin"),
        resultSet.getString("aircraft"),
        resultSet.getString("seat"),
        resultSet.getString("reference"),
        resultSet.getString("into")
    )

    fun asDaoMap() = mapOf(
        "id" to id,
        "origin" to origin.toUpperCase(),
        "destination" to destination.toUpperCase(),
        "departure" to departure,
        "arrival" to arrival,
        "carrier" to carrier.toUpperCase(),
        "number" to number,
        "cabin" to (cabin ?: "economy"),
        "aircraft" to aircraft?.toUpperCase(),
        "seat" to seat?.toUpperCase(),
        "reference" to reference,
        "into" to into
    )
}
