package com.inteli.telemetria.messaging.processor

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.inteli.telemetria.Utils.formartUrl
import com.inteli.telemetria.dto.SensorDataDTO
import java.sql.Connection
import java.sql.DriverManager

class TelemetriaProcessor(
    private val endpoint: String = System.getenv("RDS_ENDPOINT") ?: "",
    private val usuario: String = System.getenv("RDS_USUARIO") ?: "",
    private val senha: String = System.getenv("RDS_SENHA") ?: "",
    private val dbname: String = System.getenv("RDS_DBNAME") ?: "",
) {

    companion object {
        private val mapper = ObjectMapper().registerKotlinModule()
    }

    private var connection: Connection? = null

    private fun getOrCreateConnection(): Connection {
        val conn = connection

        if (conn != null && !conn.isClosed && conn.isValid(2)) {
            return conn
        }

        return DriverManager.getConnection(formartUrl(endpoint, dbname), usuario, senha).also {
            connection = it
        }
    }

    fun processBatch(messages: List<SQSMessage>, context: Context) {
        if (messages.isEmpty()) return

        val dtos = messages.mapNotNull { msg ->
            try {
                mapper.readValue(msg.body, SensorDataDTO::class.java)
            } catch (e: Exception) {
                context.logger.log("ERROR parsing message: ${msg.messageId} — ${e.message}")
                null
            }
        }

        if (dtos.isEmpty()) return

        try {
            val conn = getOrCreateConnection()

            conn.autoCommit = false

            conn.prepareStatement(
                "INSERT INTO sensor_data (sensor_id, temperatura, umidade, timestamp) VALUES (?, ?, ?, ?)"
            ).use { pstmt ->
                for (dto in dtos) {
                    pstmt.setString(1, dto.sensorId)
                    pstmt.setDouble(2, dto.temperatura)
                    pstmt.setDouble(3, dto.umidade)
                    pstmt.setString(4, dto.timestamp)
                    pstmt.addBatch()
                }
                pstmt.executeBatch()
            }

            conn.commit()
            conn.autoCommit = true

            context.logger.log("Batch of ${dtos.size} messages inserted successfully")
        } catch (e: Exception) {

            context.logger.log("ERROR on batch insert: ${e.message}")
            try {
                connection?.rollback()
                connection?.autoCommit = true
            } catch (rollbackEx: Exception) {
                context.logger.log("ERROR on rollback: ${rollbackEx.message}")
            }

            connection = null
            throw e
        }
    }
}