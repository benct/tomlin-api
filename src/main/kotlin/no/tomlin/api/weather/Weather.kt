package no.tomlin.api.weather

import com.fasterxml.jackson.annotation.JsonProperty

data class Weather(val properties: WeatherProperties) {

    data class WeatherProperties(val meta: WeatherMeta, val timeseries: List<WeatherSeries>)

    data class WeatherMeta(val updatedAt: String, val units: WeatherUnits)

    data class WeatherUnits(
        val airPressureAtSeaLevel: String,
        val airTemperature: String,
        val cloudAreaFraction: String,
        val precipitationAmount: String,
        val relativeHumidity: String,
        val windFromDirection: String,
        val windSpeed: String
    )

    data class WeatherSeries(val time: String, val data: WeatherData)

    data class WeatherData(
        val instant: WeatherInstant,
        @JsonProperty("next_1_hours") val next1Hours: WeatherNextHours? = null,
        @JsonProperty("next_6_hours") val next6Hours: WeatherNextHours? = null
    )

    data class WeatherInstant(val details: WeatherDetails)

    data class WeatherDetails(
        val airPressureAtSeaLevel: Double,
        val airTemperature: Double,
        val cloudAreaFraction: Double,
        val relativeHumidity: Double,
        val windFromDirection: Double,
        val windSpeed: Double
    )

    data class WeatherNextHours(
        val summary: WeatherNextHoursSummary,
        val details: WeatherNextHoursDetails
    )

    data class WeatherNextHoursSummary(val symbolCode: String)

    data class WeatherNextHoursDetails(val precipitationAmount: Double)
}
