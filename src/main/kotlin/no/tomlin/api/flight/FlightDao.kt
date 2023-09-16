package no.tomlin.api.flight

import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.TABLE_FLIGHT
import no.tomlin.api.flight.entity.Flight
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class FlightDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun getFlights(): List<Flight> = jdbc.query(
        Select(from = TABLE_FLIGHT, orderBy = OrderBy("departure")),
        Flight.rowMapper,
    )

    fun saveFlight(flight: Flight): Boolean = jdbc.update(
        Upsert(TABLE_FLIGHT, flight.asDaoMap())
    )

    fun deleteFlight(id: Long): Boolean = jdbc.update(
        Delete(TABLE_FLIGHT, Where("id" to id))
    )
}
