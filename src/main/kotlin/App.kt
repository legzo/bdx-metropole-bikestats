package org.jtelabs.bikestats

import org.http4k.core.Method.GET
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {

    val dataFetcher = DataFetcherImpl()
    val meterService = MeterServiceImpl(dataFetcher)
    val controller = Controller(meterService)

    val app = routes(
        "/api" bind routes(
            "data" bind GET to controller::getMeterData
        )
    )

    app.asServer(Jetty()).start().block()

}