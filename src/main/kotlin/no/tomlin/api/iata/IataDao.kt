package no.tomlin.api.iata

import no.tomlin.api.common.Constants.TABLE_AIRLINE
import no.tomlin.api.common.Constants.TABLE_LOCATION
import no.tomlin.api.iata.entity.Airline
import no.tomlin.api.iata.entity.Location
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class IataDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun getAirlines(code: String): List<Airline> =
        jdbcTemplate.query(
            "SELECT * FROM $TABLE_AIRLINE WHERE iataCode = :code",
            mapOf("code" to code)
        ) { resultSet, _ -> Airline(resultSet) }

    fun getLocations(code: String): List<Location> =
        jdbcTemplate.query(
            "SELECT * FROM $TABLE_LOCATION WHERE iataCode = :code OR cityCode = :code ORDER BY `type`",
            mapOf("code" to code)
        ) { resultSet, _ -> Location(resultSet) }

    fun batchAirlines(airlines: List<Airline>): IntArray =
        jdbcTemplate.batchUpdate(
            "INSERT INTO $TABLE_AIRLINE (id, iataCode, icaoCode, name, alias, type, started, ended, wiki) " +
                "VALUES (:id, :iataCode, :icaoCode, :name, :alias, :type, :started, :ended, :wiki)",
            airlines.map { it.asDaoMap() }.toTypedArray()
        )

    fun batchLocations(locations: List<Location>): IntArray =
        jdbcTemplate.batchUpdate(
            "INSERT INTO $TABLE_LOCATION (id, iataCode, icaoCode, cityCode, cityName, name, area, areaCode, " +
                "country, countryCode, continent, type, latitude, longitude, timezone, operational, wiki) " +
                "VALUES (:id, :iataCode, :icaoCode, :cityCode, :cityName, :name, :area, :areaCode, " +
                ":country, :countryCode, :continent, :type, :latitude, :longitude, :timezone, :operational, :wiki)",
            locations.map { it.asDaoMap() }.toTypedArray()
        )

    fun deleteAirlines(): Int = jdbcTemplate.update("DELETE FROM $TABLE_AIRLINE", EmptySqlParameterSource.INSTANCE)

    fun deleteLocations(): Int = jdbcTemplate.update("DELETE FROM $TABLE_LOCATION", EmptySqlParameterSource.INSTANCE)
}