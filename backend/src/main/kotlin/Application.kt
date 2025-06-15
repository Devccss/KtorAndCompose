package com.example

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
