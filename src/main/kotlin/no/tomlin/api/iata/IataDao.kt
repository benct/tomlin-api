package no.tomlin.api.iata

import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.TABLE_IATA_AIRLINE
import no.tomlin.api.db.Table.TABLE_IATA_LOCATION
import no.tomlin.api.iata.entity.Airline
import no.tomlin.api.iata.entity.Location
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class IataDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun getAirlines(code: String): List<Airline> = jdbc.query(
        Select(TABLE_IATA_AIRLINE)
            .where("iataCode").eq(code)
            .or("icaoCode").eq(code)
            .orderBy("iataCode", "operational"),
        Airline.rowMapper,
    )

    fun getLocations(code: String): List<Location> = jdbc.query(
        Select(TABLE_IATA_LOCATION)
            .where("iataCode").eq(code)
            .or("cityCode").eq(code)
            .or("icaoCode").eq(code)
            .orderBy("cityCode", "type", "operational"),
        Location.rowMapper,
    )

    fun searchAirlines(query: String): List<Airline> = jdbc.query(
        Select(TABLE_IATA_AIRLINE)
            .columns("*")
            .column("CONCAT_WS(' ', iataCode, icaoCode, name, alias)").custom("search")
            .having("search" to "%${query.split(" ").joinToString("%")}%")
            .orderBy("iataCode", "operational"),
        Airline.rowMapper,
    )

    fun searchLocations(query: String): List<Location> = jdbc.query(
        Select(TABLE_IATA_LOCATION)
            .columns("*")
            .column("CONCAT_WS(' ', iataCode, icaoCode, cityCode, name, cityName, country, countryCode)").custom("search")
            .having("search" to "%${query.split(" ").joinToString("%")}%")
            .orderBy("cityCode"),
        Location.rowMapper,
    )

    fun batchAirlines(airlines: List<Airline>): IntArray = jdbc.batchUpdate(
        "INSERT INTO $TABLE_IATA_AIRLINE (${Airline.keys.joinToString { "`$it`" }}) " +
            "VALUES (${Airline.keys.joinToString { ":$it" }})",
        airlines.map { it.asDaoMap() }.toTypedArray()
    )

    fun batchLocations(locations: List<Location>): IntArray = jdbc.batchUpdate(
        "INSERT INTO $TABLE_IATA_LOCATION (${Location.keys.joinToString { "`$it`" }}) " +
            "VALUES (${Location.keys.joinToString { ":$it" }})",
        locations.map { it.asDaoMap() }.toTypedArray()
    )

    fun deleteAirlines(): Boolean = jdbc.update(Delete(TABLE_IATA_AIRLINE), minimumRowsAffected = 0)

    fun deleteLocations(): Boolean = jdbc.update(Delete(TABLE_IATA_LOCATION), minimumRowsAffected = 0)
}