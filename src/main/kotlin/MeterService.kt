package org.jtelabs.bikestats

import WeatherInfo
import org.jtelabs.bikestats.backends.BikeUsageFetcher
import org.jtelabs.bikestats.backends.WeatherReportFetcher
import org.jtelabs.bikestats.models.FeatureCollection
import org.jtelabs.bikestats.models.FeatureProperties
import org.jtelabs.bikestats.models.GeoCoordinates
import org.jtelabs.bikestats.models.MeterId
import org.jtelabs.bikestats.models.MeterInfo
import org.jtelabs.bikestats.models.MeterMetric
import org.jtelabs.bikestats.models.RawMeterInfo
import org.jtelabs.bikestats.models.VisualMeterInfo
import org.jtelabs.bikestats.models.ZoneId
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt


interface MeterService {
    fun getVisualMeterData(date: LocalDate, meterId: String?): Collection<VisualMeterInfo>
    fun getRawMeterData(date: LocalDate): Collection<RawMeterInfo>
    fun getRawMeterData(startDate: LocalDate, endDate: LocalDate): Collection<RawMeterInfo>
}

class MeterServiceImpl(
    private val bikeUsageFetcher: BikeUsageFetcher,
    private val weatherReportFetcher: WeatherReportFetcher
) : MeterService {

    override fun getVisualMeterData(
        date: LocalDate,
        meterId: String?
    ): Collection<VisualMeterInfo> {

        val featureCollection = bikeUsageFetcher.fetchDataFor(
            startDate = date,
            endDate = date.plusDays(1),
            meterId = meterId?.let { MeterId(it) }
        )

        return featureCollection
            .toMeterData()
            .map { it.toVisual() }
    }

    override fun getRawMeterData(
        date: LocalDate
    ): Collection<RawMeterInfo> =
        getRawMeterData(date, date.plusDays(1))

    override fun getRawMeterData(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Collection<RawMeterInfo> {
        val featureCollection = bikeUsageFetcher.fetchDataFor(
            startDate = startDate,
            endDate = endDate,
            meterId = null
        )

        val weatherInfo: Collection<WeatherInfo> = weatherReportFetcher.getWeatherData(
            startDate = startDate,
            endDate = endDate
        )

        return featureCollection
            .toMeterData()
            .map { it.toRaw(weatherInfo) }
    }

    private fun FeatureCollection.toMeterData(): Collection<MeterInfo> {
        return features
            .groupBy { it.properties.id }
            .map { (_, featuresForAMeter) ->
                val allMetrics = featuresForAMeter.map {
                    it.properties.toMeterMetric()
                }

                with(featuresForAMeter.first()) {
                    MeterInfo(
                        id = MeterId(properties.id),
                        zone = ZoneId(properties.zone),
                        geoCoordinates = GeoCoordinates(
                            latitude = geometry.coordinates[0],
                            longitude = geometry.coordinates[1]
                        ),
                        metrics = allMetrics
                    )
                }
            }
    }

    private fun MeterInfo.toVisual() =
        VisualMeterInfo(
            id = id,
            zone = zone,
            geoCoordinates = geoCoordinates,
            metricsAsGraph = metrics.toGraph()
        )

    private fun MeterInfo.toRaw(weatherInfo: Collection<WeatherInfo>) =
        RawMeterInfo(
            id = id,
            zone = zone,
            geoCoordinates = geoCoordinates,
            times = metrics.toTimes(),
            values = metrics.toValues(),
            weatherValues = weatherInfo.getForMetrics(metrics)
        )

    private val hourFormatter = DateTimeFormatter.ofPattern("HH")
    private val decimalFormat = DecimalFormat("00.00")

    private fun List<MeterMetric>.toGraph(): List<String> {
        val max = maxOf { it.bikesPer5Minutes }

        return map { (value, time) ->
            val numberOfSymbols = if (max == 0f) 0 else (value / max * 20).roundToInt()
            val formattedValue = decimalFormat.format(value)
            "${hourFormatter.format(time)}h -> [$formattedValue] : ${"#".repeat(numberOfSymbols)}"
        }
    }

    private fun List<MeterMetric>.toTimes() =
        map { it.time.toEpochSecond() }

    private fun List<MeterMetric>.toValues() =
        map { (it.bikesPer5Minutes * 12).roundToInt() }

    private fun FeatureProperties.toMeterMetric(): MeterMetric {
        return MeterMetric(
            bikesPer5Minutes = comptage5m.roundOff2Decimals(),
            time = time
        )
    }

    private fun Float.roundOff2Decimals() = (this * 100).roundToInt() / 100f

    private fun Collection<WeatherInfo>.getForMetrics(metrics: List<MeterMetric>): List<Float?> {
        return metrics.map {
            val timeslot = it.time.toLocalDateTime()
            val correspondingWeatherInfo = firstOrNull { weatherInfo ->
                weatherInfo.fields.time.toLocalDateTime() == timeslot
            }

            correspondingWeatherInfo?.fields?.rainLastHour?.let { value -> max(value, 0f) }
        }
    }
}


