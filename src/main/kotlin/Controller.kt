package org.jtelabs.bikestats

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.auto
import org.jtelabs.bikestats.models.MeterInfo
import org.jtelabs.bikestats.models.VisualMeterInfo
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDate

class Controller(
    private val meterService: MeterService
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val meterDataLens = Body.auto<Collection<MeterInfo>>().toLens()
    private val visualMeterDataLens = Body.auto<Collection<VisualMeterInfo>>().toLens()

    fun getMeterData(
        request: Request
    ): Response {
        val meterId = request.query("meterId")
        val mode = request.query("mode")
        val date = request.getDate() ?: run {
            logger.error("Date is malformed : input was: ${request.query("date")}")
            return Response(Status.BAD_REQUEST)
        }
        logger.info("Call to /api/data with params : meterId=$meterId, date=$date")

        return when (mode) {
            "visual" -> {
                val meterData = meterService.getVisualMeterData(date, meterId)
                visualMeterDataLens.inject(meterData, Response(Status.OK))
            }
            else -> {
                val meterData = meterService.getMeterData(date, meterId)
                meterDataLens.inject(meterData, Response(Status.OK))
            }
        }

    }

    private fun Request.getDate() =
        try {
            val dateAsString = query("date")
            LocalDate.parse(dateAsString)
        } catch (exception: Exception) {
            null
        }
}