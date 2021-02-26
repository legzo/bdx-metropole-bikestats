package org.jtelabs.bikestats

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.format.Jackson.auto
import org.jtelabs.bikestats.models.FeatureCollection
import org.jtelabs.bikestats.models.MeterId
import org.slf4j.LoggerFactory
import java.time.LocalDate

interface DataFetcher {
    fun fetchDataFor(date: LocalDate, meterId: MeterId? = null): FeatureCollection
}

class DataFetcherImpl : DataFetcher {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val httpClient = OkHttp()
    private val messageLens = Body.auto<FeatureCollection>().toLens()
    private val apiKey = EnvironmentKey.required("API_KEY").extract(Environment.ENV)

    override fun fetchDataFor(
        date: LocalDate,
        meterId: MeterId?
    ): FeatureCollection {
        val request = Request(Method.GET, "https://data.bordeaux-metropole.fr/geojson/aggregate/pc_captv_p")
            .query("rangeStart", "${date}T00:00:00+00:00")
            .query("rangeEnd", "${date.plusDays(1)}T00:00:00+00:00")
            .query("rangeStep", "hour")
            .query("key", apiKey)
            .filterFor(meterId)
            .header("accept", "application/json")

        val calledUrl = request.uri.toString()
            .replace(Regex("""key=[^&]+"""), "key=xxxx")

        logger.info("Calling ${request.method} $calledUrl")

        val response = httpClient(request)

        return messageLens(response)
    }

    private fun Request.filterFor(meterId: MeterId?): Request {
        return when (meterId) {
            null -> this
            else -> this.query("filter", """{"ident":"$meterId"}""")
        }
    }
}



