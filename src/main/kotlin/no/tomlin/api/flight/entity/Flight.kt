package no.tomlin.api.flight.entity

import org.springframework.jdbc.core.RowMapper

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

    companion object {
        val rowMapper = RowMapper<Flight> { rs, _ ->
            Flight(
                rs.getLong("id"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getString("departure"),
                rs.getString("arrival"),
                rs.getString("carrier"),
                rs.getString("number"),
                rs.getString("cabin"),
                rs.getString("aircraft"),
                rs.getString("seat"),
                rs.getString("reference"),
                rs.getString("info")
            )
        }
    }
}
