package no.tomlin.api.flight

import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.TABLE_FLIGHT
import no.tomlin.api.db.Upsert
import no.tomlin.api.flight.entity.Flight
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class FlightDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun getFlights(): List<Flight> = jdbc.query(
        Select(TABLE_FLIGHT).orderBy("departure"),
        Flight.rowMapper,
    )

    fun saveFlight(flight: Flight): Boolean = jdbc.update(
        Upsert(TABLE_FLIGHT).data(flight.asDaoMap())
    )

    fun deleteFlight(id: Long): Boolean = jdbc.update(
        Delete(TABLE_FLIGHT).where("id").eq(id)
    )
}
