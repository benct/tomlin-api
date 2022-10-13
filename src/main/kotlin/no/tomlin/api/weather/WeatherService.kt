package no.tomlin.api.weather

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import no.tomlin.api.common.JsonUtils.parseJson
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.*

@Service
class WeatherService(val fetcher: HttpFetcher = fetcher(BASE_URL)) {

    @Cacheable("weather")
    fun getWeather(): Map<String, Any?> {
        val response = fetcher.get(
            headers = mapOf("User-Agent" to USER_AGENT),
            queryParams = mapOf("lat" to LAT.toString(), "lon" to LON.toString())
        )
            .readBody()
            .parseJson<Weather>(SNAKE_CASE)

        val data = response.properties.timeseries.sortedBy { it.time }.first().data

        return mapOf(
            "updated" to response.properties.meta.updatedAt,
            "temperature" to data.instant.details.airTemperature,
            "humidity" to data.instant.details.relativeHumidity,
            "clouds" to data.instant.details.cloudAreaFraction,
            "wind" to data.instant.details.windSpeed,
            "direction" to data.instant.details.windFromDirection,
            "forecast" to data.next1Hours?.summary?.symbolCode,
            "rain" to data.next1Hours?.details?.precipitationAmount,
            "units" to response.properties.meta.units,
        )
    }

    companion object {
        const val USER_AGENT = "tomlin-api github.com/benct/tomlin-api"
        const val BASE_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact"
        const val LAT = 59.925649
        const val LON = 10.762659
    }
}