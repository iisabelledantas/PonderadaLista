package com.inteli.telemetria.messaging.consumer

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.inteli.telemetria.messaging.processor.TelemetriaProcessor
import com.amazonaws.services.lambda.runtime.LambdaLogger
import org.mockito.Mockito.`when`
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.Test
import java.sql.Connection
import java.sql.PreparedStatement
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)

class TelemetriaConsumerTest {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var logger: LambdaLogger

    @Mock
    lateinit var connection: Connection

    @Mock
    lateinit var preparedStatement: PreparedStatement

    lateinit var processor: TelemetriaProcessor

    @BeforeEach
    fun setup() {
        `when`(context.logger).thenReturn(logger)
        processor = TelemetriaProcessor(
            endpoint = "localhost",
            usuario = "postgres",
            senha = "senha123",
            dbname = "telemetria",
            connectionProvider = { connection }
        )
    }

    @Test
    fun `deve processar mensagem valida com sucesso`() {
        `when`(connection.prepareStatement(any())).thenReturn(preparedStatement)
        val msg = SQSEvent.SQSMessage()
        msg.body = """{"sensorId": "sensor-001", "temperatura": 25.5, "umidade": 60.0, "timestamp": "2024-01-01T00:00:00"}"""

        assertDoesNotThrow {
            processor.processMessage(msg, context)
        }
    }

    @Test
    fun `deve processar mensagem valida com erro`() {
        val msg = SQSEvent.SQSMessage()
        msg.body = ""

        assertThrows<Exception> {
            processor.processMessage(msg, context)
        }
    }

    @Test
    fun `deve executar o insert no banco ao processar mensagem valida`() {
        `when`(connection.prepareStatement(any())).thenReturn(preparedStatement)

        val msg = SQSEvent.SQSMessage()
        msg.body = """{"sensorId": "sensor-001", "temperatura": 25.5, "umidade": 60.0, "timestamp": "2024-01-01T00:00:00"}"""

        processor.processMessage(msg, context)

        verify(preparedStatement).executeUpdate()
    }

}