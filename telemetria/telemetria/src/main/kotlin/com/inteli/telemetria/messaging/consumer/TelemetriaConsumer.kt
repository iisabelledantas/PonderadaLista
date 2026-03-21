package com.inteli.telemetria.messaging.consumer

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.inteli.telemetria.messaging.processor.TelemetriaProcessor

class TelemetriaConsumer : RequestHandler<SQSEvent, Unit> {

    private val processor = TelemetriaProcessor()

    override fun handleRequest(event: SQSEvent, context: Context) {
        context.logger.log("Received batch of ${event.records.size} messages")

        processor.processBatch(event.records, context)

        context.logger.log("Done")
    }
}