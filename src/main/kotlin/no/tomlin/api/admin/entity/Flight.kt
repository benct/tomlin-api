package no.tomlin.api.admin.entity

import no.tomlin.api.common.Constants.TABLE_FLIGHT
import java.sql.ResultSet

data class Flight(
    val id: Long?,
    val origin: String,
    val destination: String,
    val departure: String,
    val arrival: String,
    val carrier: String,
    val number: String,
    val cabin: String?,
    val aircraft: String?,
    val seat: String?,
    val reference: String,
    val info: String?
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getLong("id"),
        resultSet.getString("origin"),
        resultSet.getString("destination"),
        resultSet.getString("departure"),
        resultSet.getString("arrival"),
        resultSet.getString("carrier"),
        resultSet.getString("number"),
        resultSet.getString("cabin"),
        resultSet.getString("aircraft"),
        resultSet.getString("seat"),
        resultSet.getString("reference"),
        resultSet.getString("info")
    )

    private val keys = asDaoMap().keys

    fun asDaoMap() = mapOf(
        "id" to id,
        "origin" to origin.uppercase(),
        "destination" to destination.uppercase(),
        "departure" to departure,
        "arrival" to arrival,
        "carrier" to carrier.uppercase(),
        "number" to number,
        "cabin" to (cabin ?: "economy"),
        "aircraft" to aircraft?.uppercase(),
        "seat" to seat?.uppercase(),
        "reference" to reference,
        "info" to info
    )

    fun insertStatement(): String =
        "INSERT INTO $TABLE_FLIGHT (${keys.joinToString { "`${it}`" }}) VALUES (${keys.joinToString { ":$it" }}) " +
            "ON DUPLICATE KEY UPDATE ${keys.joinToString { "`${it}` = :${it}" }}"
}
