package com.example.routes
import DifficultyLevel
import LevelCreationDTO
import LevelUpdateDTO
import services.LevelService

import services.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Route.levelRoutes() {
    val levelService by inject<LevelService>()

    route("/levels") {
        // GET /levels - Obtener todos los niveles
        get("/test-levels") {
            call.respond(mapOf("status" to "works!"))
        }

        get {
            val levels = levelService.getAllLevels()
            call.respond(levels)
        }

        // GET /levels/{id} - Obtener un nivel por ID
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")
            val level = levelService.getLevelById(id)
            call.respond(level)
        }

        // POST /levels - Crear nuevo nivel
        post {
            val level = call.receive<LevelCreationDTO>()
            val createdLevel = levelService.createLevel(level)
            call.respond(HttpStatusCode.Created, createdLevel)
        }

        // PUT /levels/{id} - Actualizar nivel
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")
            val level = call.receive<LevelUpdateDTO>()
            val updatedLevel = levelService.updateLevel(id, level)
            call.respond(updatedLevel)
        }

        // DELETE /levels/{id} - Eliminar nivel
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")
            val success = levelService.deleteLevel(id)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                throw NotFoundException("Level not found")
            }
        }

        // GET /levels/difficulty/{level} - Obtener niveles por dificultad
        get("difficulty/{level}") {
            val difficulty = call.parameters["level"]?.let { levelStr ->
                try {
                    DifficultyLevel.valueOf(levelStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }
            } ?: throw BadRequestException("Invalid difficulty level")

            val levels = levelService.getLevelsByDifficulty(difficulty)
            call.respond(levels)
        }
    }
}