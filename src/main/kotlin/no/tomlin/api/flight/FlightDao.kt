package no.tomlin.api.flight

import no.tomlin.api.common.Constants.TABLE_FLIGHT
import no.tomlin.api.common.Extensions.checkRowsAffected
import no.tomlin.api.flight.entity.Flight
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class FlightDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun getFlights(): List<Flight> =
        jdbcTemplate.query("SELECT * FROM $TABLE_FLIGHT ORDER BY departure ASC") { resultSet, _ -> Flight(resultSet) }

    fun saveFlight(flight: Flight): Boolean = jdbcTemplate
        .update(flight.insertStatement(), flight.asDaoMap())
        .checkRowsAffected()

    fun deleteFlight(id: Long): Boolean = jdbcTemplate
        .update("DELETE FROM $TABLE_FLIGHT WHERE id = :id", mapOf("id" to id))
        .checkRowsAffected()
}
