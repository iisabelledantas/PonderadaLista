package com.inteli.telemetria.messaging.consumer

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.inteli.telemetria.messaging.processor.TelemetriaProcessor
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.sql.Connection
import java.sql.PreparedStatement

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
        lenient().`when`(context.logger).thenReturn(logger)
        lenient().`when`(connection.prepareStatement(any())).thenReturn(preparedStatement)
        lenient().`when`(connection.isClosed).thenReturn(false)
        lenient().`when`(connection.isValid(any())).thenReturn(true)

        processor = TelemetriaProcessor(
            endpoint = "localhost",
            usuario = "postgres",
            senha = "senha123",
            dbname = "telemetria",
            connectionProvider = { connection }
        )
    }

    private fun sqsMessage(body: String): SQSEvent.SQSMessage {
        return SQSEvent.SQSMessage().also { it.body = body }
    }

    private val validBody = """
        {"sensorId": "sensor-001", "temperatura": 25.5, "umidade": 60.0, "timestamp": "2024-01-01T00:00:00"}
    """.trimIndent()

    @Test
    fun `deve processar batch com mensagem valida sem lancar excecao`() {
        val messages = listOf(sqsMessage(validBody))

        assertDoesNotThrow {
            processor.processBatch(messages, context)
        }
    }

    @Test
    fun `deve executar batch insert ao processar mensagem valida`() {
        val messages = listOf(sqsMessage(validBody))

        processor.processBatch(messages, context)

        verify(preparedStatement, times(1)).addBatch()
        verify(preparedStatement, times(1)).executeBatch()
    }

    @Test
    fun `deve executar batch insert para cada mensagem valida no lote`() {
        val messages = listOf(
            sqsMessage(validBody),
            sqsMessage("""{"sensorId": "sensor-002", "temperatura": 30.0, "umidade": 70.0, "timestamp": "2024-01-02T00:00:00"}"""),
            sqsMessage("""{"sensorId": "sensor-003", "temperatura": 22.0, "umidade": 55.0, "timestamp": "2024-01-03T00:00:00"}"""),
        )

        processor.processBatch(messages, context)

        verify(preparedStatement, times(3)).addBatch()
        verify(preparedStatement, times(1)).executeBatch()
    }

    @Test
    fun `deve descartar mensagem com body invalido sem lancar excecao`() {
        val messages = listOf(sqsMessage(""))

        assertDoesNotThrow {
            processor.processBatch(messages, context)
        }

        verify(preparedStatement, never()).addBatch()
        verify(preparedStatement, never()).executeBatch()
    }

    @Test
    fun `deve processar mensagens validas e descartar invalidas no mesmo batch`() {
        val messages = listOf(
            sqsMessage(validBody),
            sqsMessage("corpo-invalido"),
            sqsMessage("""{"sensorId": "sensor-002", "temperatura": 30.0, "umidade": 70.0, "timestamp": "2024-01-02T00:00:00"}"""), // válida
        )

        assertDoesNotThrow {
            processor.processBatch(messages, context)
        }

        verify(preparedStatement, times(2)).addBatch()
        verify(preparedStatement, times(1)).executeBatch()
    }

    @Test
    fun `deve reutilizar conexao existente em warm start`() {
        val messages = listOf(sqsMessage(validBody))
        processor.processBatch(messages, context)
        processor.processBatch(messages, context)

        verify(connection, times(2)).prepareStatement(any())
        verify(connection, never()).close()
    }

    @Test
    fun `deve lancar excecao quando executeBatch falhar`() {
        `when`(preparedStatement.executeBatch()).thenThrow(RuntimeException("DB error"))

        val messages = listOf(sqsMessage(validBody))

        assertThrows<Exception> {
            processor.processBatch(messages, context)
        }
        verify(connection).autoCommit = false
    }

    @Test
    fun `nao deve fazer nada com lista vazia`() {
        assertDoesNotThrow {
            processor.processBatch(emptyList(), context)
        }

        verify(connection, never()).prepareStatement(any())
    }
}