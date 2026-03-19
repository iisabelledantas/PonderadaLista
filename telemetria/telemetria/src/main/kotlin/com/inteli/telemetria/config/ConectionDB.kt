package com.inteli.telemetria.config
import java.sql.Connection
import java.sql.DriverManager

fun getConnection(
    url: String,
    user: String,
    password: String
): Connection {
    return DriverManager.getConnection(url, user, password)
}