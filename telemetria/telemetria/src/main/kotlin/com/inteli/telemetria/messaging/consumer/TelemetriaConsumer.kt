package com.inteli.telemetria.messaging.consumer

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.inteli.telemetria.messaging.processor.TelemetriaProcessor


class TelemetriaConsumer: RequestHandler<SQSEvent, Unit> {

    val processor = TelemetriaProcessor()

    override fun handleRequest(event: SQSEvent, context: Context) {
        for (msg in event.records) {
            processor.processMessage(msg, context)
        }
        context.logger.log("done")
    }

}

