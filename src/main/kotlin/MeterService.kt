package org.jtelabs.bikestats

import org.jtelabs.bikestats.models.FeatureCollection
import org.jtelabs.bikestats.models.FeatureProperties
import org.jtelabs.bikestats.models.GeoCoordinates
import org.jtelabs.bikestats.models.MeterId
import org.jtelabs.bikestats.models.MeterInfo
import org.jtelabs.bikestats.models.MeterMetric
import org.jtelabs.bikestats.models.VisualMeterInfo
import org.jtelabs.bikestats.models.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import java.text.DecimalFormat




interface MeterService {
    fun getMeterData(date: LocalDate, meterId: String?): Collection<MeterInfo>
    fun getVisualMeterData(date: LocalDate, meterId: String?): Collection<VisualMeterInfo>
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

    private fun FeatureProperties.toMeterMetric(): MeterMetric {
        return MeterMetric(
            bikesPer5Minutes = comptage5m.roundOff2Decimals(),
            time = time
        )
    }

    private fun Float.roundOff2Decimals() = (this * 100).roundToInt() / 100f

}

