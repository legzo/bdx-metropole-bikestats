package org.jtelabs.bikestats

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.auto
import org.jtelabs.bikestats.models.MeterInfo
import org.jtelabs.bikestats.models.RawMeterInfo
import org.jtelabs.bikestats.models.VisualMeterInfo
import org.slf4j.LoggerFactory
import java.time.LocalDate

class Controller(
    private val meterService: MeterService
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val meterDataLens = Body.auto<Collection<MeterInfo>>().toLens()
    private val rawDataLens = Body.auto<Collection<RawMeterInfo>>().toLens()
    private val visualMeterDataLens = Body.auto<Collection<VisualMeterInfo>>().toLens()

    fun getRawMeterData(
        request: Request
    ): Response {
        val date = request.getDateParam("date")

        val rawMeterData = if (date != null) {
            logger.info("Call to /api/data with params : date=$date")
            meterService.getRawMeterData(date)
        } else {
            val startDate = request.getDateParam("startDate")
            val endDate = request.getDateParam("endDate")

            if (startDate == null || endDate == null) {
                logger.error("Date is malformed : input was: ${request.query("date")}")
                return Response(Status.BAD_REQUEST)
            }

            logger.info("Call to /api/data with params : startDate=$startDate, endDate=$endDate")
            meterService.getRawMeterData(startDate, endDate)
        }

        return rawDataLens.inject(rawMeterData, Response(Status.OK))
    }

    fun getMeterData(
        request: Request
    ): Response {
        val meterId = request.query("meterId")
        val mode = request.query("mode")
        val date = request.getDateParam("date") ?: run {
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

    private fun Request.getDateParam(paramName: String) =
        try {
            val dateAsString = query(paramName)
            LocalDate.parse(dateAsString)
        } catch (exception: Exception) {
            null
        }
}