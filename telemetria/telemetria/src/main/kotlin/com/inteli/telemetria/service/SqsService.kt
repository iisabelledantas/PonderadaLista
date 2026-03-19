package com.inteli.telemetria.service

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

@Service
class SqsService(
    private val sqsClient: SqsClient
) {

    fun sendMessage(queueUrl: String, message: String) {

        val request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(message)
            .build()

        sqsClient.sendMessage(request)
    }
}