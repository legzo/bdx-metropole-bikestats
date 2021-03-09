package org.jtelabs.bikestats.backends

import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class WeatherReportFetcherImplTest {

    @Test
    fun `build a correct url`() {
        WeatherReportFetcherImpl.buildUrlForDates(
            startDate = LocalDate.of(2021, 3, 1),
            endDate = LocalDate.of(2021, 3, 8)
        ) shouldBe "https://public.opendatasoft.com/api/records/1.0/search/" +
                "?dataset=donnees-synop-essentielles-omm" +
                "&sort=date" +
                "&facet=date" +
                "&facet=nom" +
                "&facet=temps_present" +
                "&facet=libgeo" +
                "&facet=nom_epci" +
                "&facet=nom_dept" +
                "&facet=nom_reg" +
                "&refine.nom=BORDEAUX-MERIGNAC" +
                "&q=date%3A%5B2021-03-01T00%3A00%3A00Z+TO+2021-03-08T00%3A00%3A00Z%5D"
    }

    @Test
    @Disabled
    fun `get data`() {
        val weatherReportFetcher = WeatherReportFetcherImpl()

        val weatherData = weatherReportFetcher.getWeatherData(
            startDate = LocalDate.of(2021, 3, 1),
            endDate = LocalDate.of(2021, 3, 8)
        )

        weatherData shouldHaveAtLeastSize 3
    }
}