package com.example

import com.example.Routes.configureRouting
import com.example.config.Limits.configureAdministration
import com.example.config.configureDatabases
import com.example.config.configureFrameworks
import com.example.config.configureHTTP
import com.example.config.configureSecurity
import com.example.config.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureAdministration()
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureHTTP()
    configureRouting()

}
