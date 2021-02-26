package org.jtelabs.bikestats

import org.jtelabs.bikestats.models.FeatureCollection
import org.jtelabs.bikestats.models.FeatureProperties
import org.jtelabs.bikestats.models.GeoCoordinates
import org.jtelabs.bikestats.models.MeterId
import org.jtelabs.bikestats.models.MeterInfo
import org.jtelabs.bikestats.models.MeterMetric
import org.jtelabs.bikestats.models.ZoneId
import java.time.LocalDate
import kotlin.math.roundToInt

interface MeterService {
    fun getMeterData(date: LocalDate, meterId: String?): Collection<MeterInfo>
}

class MeterServiceImpl(
    private val dataFetcher: DataFetcher
): MeterService {

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

    private fun FeatureProperties.toMeterMetric(): MeterMetric {
        return MeterMetric(
            bikesPer5Minutes = comptage5m.roundOff2Decimals(),
            time = time
        )
    }

    private fun Float.roundOff2Decimals() = (this * 100).roundToInt() / 100f

}