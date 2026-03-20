package com.inteli.telemetria.controller

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.inteli.telemetria.dto.SensorDataDTO
import com.inteli.telemetria.service.SqsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class TelemetriaControllerTest {

    @Mock
    lateinit var sqsService: SqsService

    @Mock
    lateinit var objectMapper: ObjectMapper

    @InjectMocks
    lateinit var controller: TelemetriaController

    lateinit var dto: SensorDataDTO

    lateinit var queueUrl: String

    @BeforeEach
    fun setup() {
        dto = SensorDataDTO(
            sensorId = "sensor-01",
            temperatura = 20.0,
            umidade = 80.0,
            timestamp = "2026-03-18T08:00:00Z"
        )
        controller.queueUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/telemetria-queue"
    }

    @Test
    fun `deve retornar 200 OK ao receber dados válidos`() {
        `when`(objectMapper.writeValueAsString(dto)).thenReturn("""{"sensorId":"sensor-01","temperatura":20.0,"umidade":80.0,"timestamp":"2026-03-18T08:00:00Z"}""")

        val response = controller.receberDados(dto)

        assertEquals(200, response.statusCode.value())

    }

    @Test
    fun `deve retornar lancar excecao ao receber dados válidos`() {
        `when`(objectMapper.writeValueAsString(dto)).thenThrow(JsonProcessingException::class.java)

        assertThrows<Exception> {
            controller.receberDados(dto)
        }

    }

    @Test
    fun `deve chamar o sqsService ao receber dados válidos`() {
        `when`(objectMapper.writeValueAsString(dto)).thenReturn("""{"sensorId":"sensor-01","temperatura":20.0,"umidade":80.0,"timestamp":"2026-03-18T08:00:00Z"}""")

        controller.receberDados(dto)

        verify(sqsService).sendMessage(controller.queueUrl, objectMapper.writeValueAsString(dto))
    }

}