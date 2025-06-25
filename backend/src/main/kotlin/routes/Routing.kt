import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing


import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject
import services.LevelService
import services.NotFoundException


@Serializable
data class DatabaseTestResult(
    val status: String,
    val result: Int,
    val database: String
)

fun Application.configureRouting() {

    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message))
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
    }
    val levelService = get<LevelService>()

    routing {

        get("/health") {
            call.respondText("API is running!")
        }


        get("/") {
            call.respondText("Hola funciona la api!")
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
        route("/api/v1") {


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
    }
}


