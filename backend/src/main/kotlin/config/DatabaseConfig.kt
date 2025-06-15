package com.example.config

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/ktorDatabase",
        user = "Deivid_user",
        password = "-Ducli123"
    )
}