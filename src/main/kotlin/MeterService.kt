package org.jtelabs.bikestats

import org.jtelabs.bikestats.models.*
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


interface MeterService {
    fun getMeterData(date: LocalDate, meterId: String?): Collection<MeterInfo>
    fun getVisualMeterData(date: LocalDate, meterId: String?): Collection<VisualMeterInfo>
    fun getRawMeterData(date: LocalDate): Collection<RawMeterInfo>
    fun getRawMeterData(startDate: LocalDate, endDate: LocalDate): Collection<RawMeterInfo>
}

class MeterServiceImpl(
    private val dataFetcher: DataFetcher
) : MeterService {

    override fun getVisualMeterData(
        date: LocalDate,
        meterId: String?
    ): Collection<VisualMeterInfo> =
        getMeterData(date, meterId)
            .map { it.toVisual() }

    override fun getRawMeterData(
        date: LocalDate
    ): Collection<RawMeterInfo> =
        getMeterData(date, null)
            .map { it.toRaw() }

    override fun getRawMeterData(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Collection<RawMeterInfo> {
        val featureCollection = dataFetcher.fetchDataFor(
            startDate = startDate,
            endDate = endDate,
            meterId = null
        )

        return featureCollection
            .toMeterData()
            .map { it.toRaw() }
    }

    override fun getMeterData(
        date: LocalDate,
        meterId: String?
    ): Collection<MeterInfo> {

        val featureCollection = dataFetcher.fetchDataFor(
            date = date,
            meterId = meterId?.let { MeterId(it) }
        )

        return featureCollection.toMeterData()
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

    private fun MeterInfo.toRaw() =
        RawMeterInfo(
            id = id,
            zone = zone,
            geoCoordinates = geoCoordinates,
            times = metrics.toTimes(),
            values = metrics.toValues()
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

}

