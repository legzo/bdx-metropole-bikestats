package org.jtelabs.bikestats

import org.http4k.core.Method.GET
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main(args: Array<String>) {

    val port = if (args.isNotEmpty()) args[0].toInt() else 8000

    val dataFetcher = DataFetcherImpl()
    val meterService = MeterServiceImpl(dataFetcher)
    val controller = Controller(meterService)

    val app = routes(
        "/api" bind routes(
            "data" bind GET to controller::getMeterData
        )
    )

    app.asServer(Undertow(port)).start().block()

}