package com.inteli.telemetria.controller

import com.inteli.telemetria.dto.SensorDataDTO
import com.inteli.telemetria.service.SqsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/telemetria")
class TelemetriaController(
    private val sqsService: SqsService
) {

    @Value("\${spring.cloud.aws.sqs.endpoint}")
    lateinit var queueUrl: String

    @PostMapping("/dados")
    fun receberDados(@RequestBody dados: SensorDataDTO): ResponseEntity<String> {

            sqsService.sendMessage(queueUrl, dados.toString())

        return ResponseEntity.ok("Dados recebidos com sucesso")
    }
}