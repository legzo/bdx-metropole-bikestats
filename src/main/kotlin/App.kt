package org.jtelabs.bikestats

import org.http4k.core.Method.GET
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.jtelabs.bikestats.backends.BikeUsageFetcherImpl
import org.jtelabs.bikestats.backends.WeatherReportFetcherImpl

fun main(args: Array<String>) {

    val port = if (args.isNotEmpty()) args[0].toInt() else 8000

    val bikeUserFetcher = BikeUsageFetcherImpl()
    val weatherReportFetcher = WeatherReportFetcherImpl()
    val meterService = MeterServiceImpl(bikeUserFetcher, weatherReportFetcher)
    val controller = Controller(meterService)

    val app = routes(
        "/api" bind routes(
            "data" bind GET to controller::getVisualMeterData,
            "raw" bind routes(
                "data" bind GET to controller::getRawMeterData
            )
        ),
        "/" bind static(Classpath("/web"))
    )

    app.asServer(Undertow(port)).start().block()

}