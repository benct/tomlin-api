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
            .where("iataCode").eq(code),
        Airline.rowMapper,
    )

    fun getLocations(code: String): List<Location> = jdbc.query(
        Select(TABLE_IATA_LOCATION)
            .where("iataCode").eq(code)
            .or("cityCode").eq(code)
            .orderBy("type"),
        Location.rowMapper,
    )

    fun batchAirlines(airlines: List<Airline>): IntArray = jdbc.batchUpdate(
        "INSERT INTO $TABLE_IATA_AIRLINE (id, iataCode, icaoCode, name, alias, type, started, ended, wiki) " +
            "VALUES (:id, :iataCode, :icaoCode, :name, :alias, :type, :started, :ended, :wiki)",
        airlines.map { it.asDaoMap() }.toTypedArray()
    )

    fun batchLocations(locations: List<Location>): IntArray = jdbc.batchUpdate(
        "INSERT INTO $TABLE_IATA_LOCATION (id, iataCode, icaoCode, cityCode, cityName, name, area, areaCode, " +
            "country, countryCode, continent, type, latitude, longitude, timezone, operational, wiki) " +
            "VALUES (:id, :iataCode, :icaoCode, :cityCode, :cityName, :name, :area, :areaCode, " +
            ":country, :countryCode, :continent, :type, :latitude, :longitude, :timezone, :operational, :wiki)",
        locations.map { it.asDaoMap() }.toTypedArray()
    )

    fun deleteAirlines(): Boolean = jdbc.update(Delete(TABLE_IATA_AIRLINE))

    fun deleteLocations(): Boolean = jdbc.update(Delete(TABLE_IATA_LOCATION))
}