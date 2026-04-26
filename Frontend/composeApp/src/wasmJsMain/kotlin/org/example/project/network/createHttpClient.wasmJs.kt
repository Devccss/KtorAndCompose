package org.example.project.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient = HttpClient(Js) {
    install(HttpTimeout) {
        requestTimeoutMillis = 120_000
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            isLenient = true
        })
    }
}
