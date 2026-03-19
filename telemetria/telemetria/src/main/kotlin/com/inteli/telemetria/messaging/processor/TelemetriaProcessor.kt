package com.inteli.telemetria.messaging.processor

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.inteli.telemetria.Utils.formartUrl
import com.inteli.telemetria.dto.SensorDataDTO
import java.sql.DriverManager.getConnection

class TelemetriaProcessor {

    val endpoint = System.getenv("RDS_ENDPOINT")
    val usuario = System.getenv("RDS_USUARIO")
    val senha = System.getenv("RDS_SENHA")
    val dbname = System.getenv("RDS_DBNAME")

    val url = formartUrl(endpoint, dbname)

    fun processMessage(msg: SQSMessage, context: Context) {
        try {

            val mapper = ObjectMapper().registerKotlinModule()
            val dto = mapper.readValue(msg.body, SensorDataDTO::class.java)

            getConnection(url, usuario, senha).use { conectionDB ->
                conectionDB.prepareStatement(
                    "INSERT INTO sensor_data (sensor_id, temperatura, umidade, timestamp) VALUES (?, ?, ?, ?)"
                ).use { pstmt ->
                    pstmt.setString(1, dto.sensorId)
                    pstmt.setDouble(2, dto.temperatura)
                    pstmt.setDouble(3, dto.umidade)
                    pstmt.setString(4, dto.timestamp)
                    pstmt.executeUpdate()
                }
            }

            context.logger.log("Processed message " + msg.body)

        } catch (e: Exception) {
            context.logger.log("An error occurred")
            throw e
        }
    }
}