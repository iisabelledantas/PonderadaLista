package com.inteli.telemetria.messaging.processor

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.inteli.telemetria.Utils.formartUrl
import com.inteli.telemetria.dto.SensorDataDTO
import java.sql.DriverManager.getConnection
import java.sql.Connection
import java.sql.DriverManager

class TelemetriaProcessor(
    private val endpoint: String = System.getenv("RDS_ENDPOINT") ?: "",
    private val usuario: String = System.getenv("RDS_USUARIO") ?: "",
    private val senha: String = System.getenv("RDS_SENHA") ?: "",
    private val dbname: String = System.getenv("RDS_DBNAME") ?: "",
    private val connectionProvider: () -> Connection = {
        DriverManager.getConnection(formartUrl(endpoint, dbname), usuario, senha)
    }
) {
    fun processMessage(msg: SQSMessage, context: Context) {
        try {
            val mapper = ObjectMapper().registerKotlinModule()
            val dto = mapper.readValue(msg.body, SensorDataDTO::class.java)

            connectionProvider().use { conectionDB ->
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