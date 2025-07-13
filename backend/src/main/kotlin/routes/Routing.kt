
import com.example.dtos.CreateDialogDTO
import com.example.dtos.UpdateDialogDTO
import com.example.services.DialogService
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
    val dialogService = get<DialogService>()

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

            route("/dialogs"){
                get {
                    val dialogs = dialogService.getAllDialogs()
                    call.respond(dialogs)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dialog = dialogService.getDialogById(id)
                    call.respond(dialog)
                }
                get("level/{levelId}") {
                    val levelId = call.parameters["levelId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Level ID")
                    val dialogs = dialogService.getDialogsByLevelId(levelId)
                    call.respond(dialogs)
                }
                post("{levelId}") {
                    val dto = call.receive<CreateDialogDTO>()
                    val idLevel = call.parameters["levelId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Level ID")
                    val dialog = dialogService.createDialog(dto,idLevel)
                    call.respond(HttpStatusCode.Created, dialog)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dialog = call.receive<UpdateDialogDTO>()
                    if (dialog.name?.isBlank() == true) {
                        throw BadRequestException("Dialog name cannot be empty")
                    }
                    if (dialog.description?.isBlank() == true) {
                        throw BadRequestException("Dialog description cannot be empty")
                    }
                    if (dialog.levelId == null) {
                        throw BadRequestException("Dialog must have a level ID")
                    }
                    dialog.levelId.let { levelService.getLevelById(it) }
                    val updatedDialog = dialogService.updateDialog(id, dialog)
                    call.respond(updatedDialog)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val success = dialogService.deleteDialog(id)
                    if (success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        throw NotFoundException("Dialog not found")
                    }
                }


            }

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
                post() {
                    val dto = call.receive<LevelCreationDTO>()
                    val beforeId = call.request.queryParameters["beforeId"]?.toIntOrNull()
                    val afterId = call.request.queryParameters["afterId"]?.toIntOrNull()

                    println("beforeId: $beforeId")
                    println("afterId: $afterId")

                    val level = levelService.createLevel(dto, beforeId, afterId)
                    call.respond(HttpStatusCode.Created, level)
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


