package com.inteli.telemetria.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.inteli.telemetria.dto.SensorDataDTO
import com.inteli.telemetria.service.SqsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/telemetria")
class TelemetriaController(
    private val sqsService: SqsService,
    private val objectMapper: ObjectMapper
) {

    @Value("\${spring.cloud.aws.sqs.endpoint}")
    lateinit var queueUrl: String

    @PostMapping("/dados")
    fun receberDados(@RequestBody dados: SensorDataDTO): ResponseEntity<String> {

        sqsService.sendMessage(queueUrl, objectMapper.writeValueAsString(dados))

        return ResponseEntity.ok("Dados recebidos com sucesso")
    }
}