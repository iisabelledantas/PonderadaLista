package com.inteli.telemetria.service

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class SqsServiceTest {

    @Mock
    lateinit var sqsClient: SqsClient

    @InjectMocks
    lateinit var sqsService: SqsService

    @Test
    fun `deve chamar o sqsClient ao enviar mensagem`() {
        sqsService.sendMessage(
            queueUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/telemetria-queue",
            message = """{"sensorId":"sensor-01","temperatura":20.0,"umidade":80.0,"timestamp":"2026-03-18T08:00:00Z"}"""
        )

        verify(sqsClient).sendMessage(any<SendMessageRequest>())
    }

    @Test
    fun `deve lancar excecao quando o sqsClient falhar`() {
        `when`(sqsClient.sendMessage(any<SendMessageRequest>())).thenThrow(RuntimeException::class.java)

        assertThrows<Exception> {
            sqsService.sendMessage(
                queueUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/telemetria-queue",
                message = """{"sensorId":"sensor-01","temperatura":20.0,"umidade":80.0,"timestamp":"2026-03-18T08:00:00Z"}"""
            )
        }
    }
}