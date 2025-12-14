import models.DifficultyLevel
import com.example.dtos.CreateDialogDTO
import com.example.dtos.CreateParticipantDTO
import com.example.dtos.CreatePhraseDto
import com.example.dtos.CreatePhraseWordDto
import com.example.dtos.CreateTestDto
import com.example.dtos.CreateUserDto
import com.example.dtos.CreateWordDto
import com.example.dtos.DialogDTOs
import com.example.dtos.DialogDetailDTO
import com.example.dtos.LoginDto
import com.example.dtos.UpdateDialogDTO
import com.example.dtos.UpdateParticipantDTO
import com.example.dtos.OrderPhraseDto
import com.example.dtos.ParticipantDetailDTO
import com.example.dtos.PhraseDetailDTO
import com.example.dtos.ProgressDto
import com.example.dtos.StandbyDto
import com.example.dtos.StandbyUpdateDto
import com.example.dtos.updateUserDto
import com.example.services.DialogParticipantsService
import com.example.services.DialogService
import com.example.services.PhraseService
import com.example.services.PhraseWordService
import com.example.services.TestService
import com.example.services.UserService
import com.example.services.WordService
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
    val phraseService = get<PhraseService>()
    val wordService = get<WordService>()
    val phraseWordService = get<PhraseWordService>()
    val userService = get<UserService>()
    val testService = get<TestService>()

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

            route("/users") {
                get {
                    val users = userService.getAllUsers()
                    call.respond(users)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val user = userService.getUserById(id)
                    if (user != null) {
                        call.respond(user)
                    } else {
                        throw NotFoundException("User not found")
                    }
                }
                get("email/{email}") {
                    val email = call.parameters["email"]
                        ?: throw BadRequestException("Email parameter is required")
                    val user = userService.getUserByEmail(email)
                    if (user != null) {
                        call.respond(user)
                    } else {
                        throw NotFoundException("User not found")
                    }
                }
                post("login") {
                    val dto = call.receive<LoginDto>()
                    val user = userService.initSesion(dto)
                    call.respond(user)
                }
                post("register") {
                    val dto = call.receive<CreateUserDto>()
                    val user = userService.createUser(dto)
                    call.respond(HttpStatusCode.Created, user)
                }

                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<updateUserDto>()
                    val updatedUser = userService.updateUser(id, dto)
                    if (updatedUser != null) {
                        call.respond(updatedUser)
                    } else {
                        throw NotFoundException("User not found")
                    }
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val success = userService.deleteUser(id)
                    call.respond(success)
                }
            }

            route("/words") {
                get {
                    val words = wordService.getAllWords()
                    call.respond(words)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val word = wordService.getWordById(id)
                    if (word != null) {
                        call.respond(word)
                    } else {
                        throw NotFoundException("Word not found")
                    }
                }
                get("phrase/{phraseId}") {
                    val phraseId = call.parameters["phraseId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Phrase ID")
                    val words = wordService.getWordsByPhraseId(phraseId)
                    call.respond(words)
                }
                post {
                    val dto = call.receive<CreateWordDto>()
                    val word = wordService.createWord(dto)
                    call.respond(HttpStatusCode.Created, word)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<CreateWordDto>()
                    val updatedWord = wordService.updateWord(id, dto)
                    if (updatedWord != null) {
                        call.respond(updatedWord)
                    } else {
                        throw NotFoundException("Word not found")
                    }
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val success = wordService.deleteWord(id)
                    if (success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        throw NotFoundException("Word not found")
                    }
                }
            }


        }
    }
}