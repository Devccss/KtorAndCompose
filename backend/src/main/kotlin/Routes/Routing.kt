package com.example.Routes


import io.ktor.http.*
import io.ktor.resources.*

import io.ktor.server.application.*

import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Serializable
data class DatabaseTestResult(
    val status: String,
    val result: Int,
    val database: String
)

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    install(Resources)
    routing {
        get("/") {
            call.respondText("Hola funciona la api!")
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        get("/test-db") {
            try {
                val result = transaction {
                    exec("SELECT 1") { rs ->
                        rs.next()
                        rs.getInt(1)
                    }
                }
                call.respond(
                    DatabaseTestResult(
                    status = "OK",
                    result = result!!,
                    database = "PostgreSQL"
                )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "error" to (e.message ?: "Unknown error"),
                        "type" to "DatabaseError"
                    )
                )
            }
        }
    }
}
@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
