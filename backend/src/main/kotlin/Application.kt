package com.example

import com.example.config.Limits.configureAdministration
import com.example.config.configureSecurity
import com.example.config.configureSerialization
import config.configureDatabases
import configureHTTP
import com.example.plugins.configureKoin
import configureRouting
import io.ktor.server.application.Application


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureAdministration()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureHTTP()
    configureRouting()

}
