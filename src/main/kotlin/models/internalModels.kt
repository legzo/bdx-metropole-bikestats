package org.jtelabs.bikestats.models

import java.time.OffsetDateTime

data class MeterInfo(
    val id: MeterId,
    val zone: ZoneId,
    val geoCoordinates: GeoCoordinates,
    val metrics: List<MeterMetric>
)

data class RawMeterInfo(
    val id: MeterId,
    val zone: ZoneId,
    val geoCoordinates: GeoCoordinates,
    val times: List<Long>,
    val values: List<Int>
)

data class VisualMeterInfo(
    val id: MeterId,
    val zone: ZoneId,
    val geoCoordinates: GeoCoordinates,
    val metricsAsGraph: List<String>
)

data class GeoCoordinates(
    val latitude: Float,
    val longitude: Float
)

data class MeterMetric(
    val bikesPer5Minutes: Float,
    val time: OffsetDateTime
)

data class RawMeterMetric(
    val bikesPer5Minutes: Float,
    val time: Long
)

inline class MeterId(private val value: String) {
    override fun toString() = value
}

inline class ZoneId(private val value: String) {
    override fun toString() = value
}