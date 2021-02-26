package org.jtelabs.bikestats.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class FeatureCollection(
    val type: String,
    val features: Collection<Feature>
)

data class Feature(
    val geometry: FeatureGeometry,
    val properties: FeatureProperties
)

data class FeatureGeometry(
    val coordinates: List<Float>
)

data class FeatureProperties(
    @JsonProperty("ident")
    val id: String,
    val zone: String,
    @JsonProperty("comptage_5m")
    val comptage5m: Float,
    val time: OffsetDateTime
)
