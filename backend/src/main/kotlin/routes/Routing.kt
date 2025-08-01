import models.DifficultyLevel
import com.example.dtos.CreateDialogDTO
import com.example.dtos.CreateParticipantDTO
import com.example.dtos.CreatePhraseDto
import com.example.dtos.CreatePhraseWordDto
import com.example.dtos.CreateUserDto
import com.example.dtos.CreateWordDto
import com.example.dtos.LoginDto
import com.example.dtos.UpdateDialogDTO
import com.example.dtos.UpdateParticipantDTO
import com.example.dtos.OrderPhraseDto
import com.example.services.DialogParticipantsService
import com.example.services.DialogService
import com.example.services.PhraseService
import com.example.services.PhraseWordService
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
    val dialogParticipants = get<DialogParticipantsService>()
    val phraseService = get<PhraseService>()
    val wordService = get<WordService>()
    val phraseWordService = get<PhraseWordService>()
    val userService = get<UserService>()

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

            route("/users"){
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
                post ("register") {
                    val dto = call.receive<CreateUserDto>()
                    val user = userService.createUser(dto)
                    call.respond(HttpStatusCode.Created, user)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<CreateUserDto>()
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
                    if (success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        throw NotFoundException("User not found")
                    }
                }
            }

            route("/words"){
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

            route("/phrases") {
                get {
                    val phrases = phraseService.getAllPhrases()
                    call.respond(phrases)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val phrase = phraseService.getPhraseById(id)
                    call.respond(phrase)
                }
                post("{participantId}") {
                    val dto = call.receive<CreatePhraseDto>()
                    val participantId = call.parameters["participantId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Participant ID")
                    val phrase = phraseService.createPhrase(dto, participantId)
                    call.respond(HttpStatusCode.Created, phrase)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<CreatePhraseDto>()
                    val updatedPhrase = phraseService.updatePhrase(id, dto)
                    call.respond(updatedPhrase)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val success = phraseService.deletePhrase(id)
                    if (success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        throw NotFoundException("Phrase not found")
                    }
                }
                put("/order"){
                    val dto = call.receive<OrderPhraseDto>()
                    val success = phraseService.orderPhrase(dto)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("status" to "Phrase ordered successfully"))
                    } else {
                        throw NotFoundException("Failed to order phrase")
                    }
                }
            }

            route("/phraseWord"){
                get(){
                    val phraseWords = phraseWordService.getAllPhraseWords()
                    call.respond(phraseWords)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val phraseWord = phraseWordService.getPhraseWordById(id)
                    if (phraseWord != null) {
                        call.respond(phraseWord)
                    } else {
                        throw NotFoundException("PhraseWord not found")
                    }
                }
                get("phrase/{phraseId}") {
                    val phraseId = call.parameters["phraseId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Phrase ID")
                    val phraseWords = phraseWordService.getPhraseWordsByPhraseId(phraseId)
                    call.respond(phraseWords)
                }
                get("word/{wordId}") {
                    val wordId = call.parameters["wordId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Word ID")
                    val phraseWords = phraseWordService.getPhraseWordsByWordId(wordId)
                    call.respond(phraseWords)
                }
                post{
                    val dto = call.receive<CreatePhraseWordDto>()
                    val phraseWord = phraseWordService.createPhraseWord(dto)
                    call.respond(HttpStatusCode.Created, phraseWord)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<CreatePhraseWordDto>()
                    val updatedPhraseWord = phraseWordService.updatePhraseWord(id, dto)
                    if (updatedPhraseWord != null) {
                        call.respond(updatedPhraseWord)
                    } else {
                        throw NotFoundException("PhraseWord not found")
                    }
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val success = phraseWordService.deletePhraseWord(id)
                    if (success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        throw NotFoundException("PhraseWord not found")
                    }
                }
            }

            route("/participants") {
                get("byId/{participantId}") {
                    val participantId = call.parameters["participantId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Participant ID")
                    val participant = dialogParticipants.getParticipantById(participantId)
                        ?: throw NotFoundException("Participant not found")
                    call.respond(participant)
                }
                get("{dialogId}") {
                    val dialogId = call.parameters["dialogId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Dialog ID")
                    val participants = dialogParticipants.getParticipantsByDialogId(dialogId)
                    call.respond(participants)
                }
                post("{dialogId}") {
                    val dto = call.receive<CreateParticipantDTO>()
                    val dialogId = call.parameters["dialogId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Dialog ID")
                    val participant = dialogParticipants.createDialogParticipant(dialogId, dto)
                    call.respond(HttpStatusCode.Created, participant)
                }
                put("{participantId}") {
                    val participantId = call.parameters["participantId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Participant ID")
                    val dto = call.receive<UpdateParticipantDTO>()
                    val updatedParticipant =
                        dialogParticipants.updateDialogParticipant(participantId, dto)
                    call.respond(updatedParticipant)
                }
                delete("{participantId}") {
                    val participantId = call.parameters["participantId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Participant ID")
                    val success = dialogParticipants.deleteDialogParticipant(participantId)
                    if (success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        throw NotFoundException("Participant not found")
                    }
                }
            }

            route("/dialogs") {
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
                    val dialog = dialogService.createDialog(dto, idLevel)
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


