package com.inteli.telemetria

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelemetriaApplication

fun main(args: Array<String>) {
	runApplication<TelemetriaApplication>(*args)
}
