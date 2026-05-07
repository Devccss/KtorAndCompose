package com.example

import com.example.config.Limits.configureAdministration
import com.example.config.configureSerialization
import com.example.routes.configureRouting
import config.configureDatabase
import config.configureHTTP
import config.configureSecurity
import io.ktor.server.application.Application
import plugins.configureKoin


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)

}



fun Application.module() {
    configureKoin()
    configureAdministration()
    configureSerialization()
    configureDatabase()
    configureSecurity()
    configureHTTP()
    configureRouting()

}
