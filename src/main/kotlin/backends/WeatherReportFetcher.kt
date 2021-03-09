package org.jtelabs.bikestats.backends

import WeatherInfo
import WeatherInfoCollection
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.format.Jackson.auto
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface WeatherReportFetcher {
    fun getWeatherData(startDate: LocalDate, endDate: LocalDate): Collection<WeatherInfo>
}

class WeatherReportFetcherImpl : WeatherReportFetcher {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = OkHttp()
    private val messageLens = Body.auto<WeatherInfoCollection>().toLens()

    override fun getWeatherData(
        startDate: LocalDate,
        endDate: LocalDate
    ): Collection<WeatherInfo> {
        val calledUrl = buildUrlForDates(startDate, endDate)

        val request = Request(Method.GET, calledUrl)
            .header("accept", "application/json")

        logger.info("Calling ${request.method} $calledUrl")

        val response = httpClient(request)

        return messageLens(response).records
    }

    companion object {
        private const val BASE_URL = "https://public.opendatasoft.com/api/records/1.0/search/" +
                "?dataset=donnees-synop-essentielles-omm" +
                "&rows=9999" +
                "&sort=date" +
                "&facet=date" +
                "&facet=nom" +
                "&facet=temps_present" +
                "&facet=libgeo" +
                "&facet=nom_epci" +
                "&facet=nom_dept" +
                "&facet=nom_reg" +
                "&refine.nom=BORDEAUX-MERIGNAC"

        fun buildUrlForDates(startDate: LocalDate, endDate: LocalDate): String {
            fun formatForUrl(startDate: LocalDate) = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val startDateAsString = formatForUrl(startDate)
            val endDateAsString = formatForUrl(endDate)

            return BASE_URL + "&q=date%3A%5B${startDateAsString}T00%3A00%3A00Z+TO+${endDateAsString}T00%3A00%3A00Z%5D"
        }

    }
}