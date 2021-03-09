import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class WeatherInfoCollection(
    val records: Collection<WeatherInfo>
)

data class WeatherInfo(
    val fields: WeatherInfoFields
)

data class WeatherInfoFields(
    @JsonProperty("rr1")
    val rainLastHour: Float,
    @JsonProperty("date")
    val time: OffsetDateTime
)