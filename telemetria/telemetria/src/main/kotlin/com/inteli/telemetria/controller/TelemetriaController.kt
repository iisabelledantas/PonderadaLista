package com.inteli.telemetria.controller

import com.inteli.telemetria.dto.SensorDataDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/telemetria")
class TelemetriaController {

    @PostMapping("/dados")
    fun receberDados(@RequestBody dados: SensorDataDTO): ResponseEntity<String> {
        println("Dados recebidos: $dados")
        return ResponseEntity.ok("Dados recebidos com sucesso")
    }
}