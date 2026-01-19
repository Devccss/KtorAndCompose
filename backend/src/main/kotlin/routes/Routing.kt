import com.example.dtos.*
import com.example.services.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
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
    
    val userService = get<UserService>()

    // Obtener el resto de servicios via Koin
    val unitService = get<UnitService>()
    val exerciseService = get<ExerciseService>()
    val contentExerciseService = get<ContentExerciseService>()
    val contentWordService = get<ContentWordService>()
    val wordService = get<WordService>()
    val questionService = get<QuestionService>()
    val testService = get<TestService>()
    val testExerciseService = get<TestExerciseService>()
    val exerciseOnHoldService = get<ExerciseOnHoldService>()
    val notificationsService = get<NotificationsService>()

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
                    val dto = call.receive<UpdateUserDto>()
                    val updatedUser = userService.updateUser(id, dto)
                    call.respond(updatedUser)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val success = userService.deleteUser(id)
                    call.respond(success)
                }
            }

            // Units
            route("/units") {
                get {
                    call.respond(unitService.getAllUnits())
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = unitService.getUnitById(id) ?: throw NotFoundException("Unit not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateUnitDto>()
                    val created = unitService.createUnit(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateUnitDto>()
                    unitService.updateUnit(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(unitService.deleteUnit(id))
                }
            }

            // Exercises
            route("/exercises") {
                get { call.respond(exerciseService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = exerciseService.getById(id) ?: throw NotFoundException("Exercise not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateExerciseDto>()
                    val created = exerciseService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseDto>()
                    exerciseService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseService.delete(id))
                }
            }

            // ContentExercises
            route("/content-exercises") {
                get { call.respond(contentExerciseService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = contentExerciseService.getById(id) ?: throw NotFoundException("ContentExercise not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateContentExerciseDto>()
                    val created = contentExerciseService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateContentExerciseDto>()
                    contentExerciseService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(contentExerciseService.delete(id))
                }
            }

            // ContentWords
            route("/content-words") {
                get { call.respond(contentWordService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = contentWordService.getById(id) ?: throw NotFoundException("ContentWord not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateContentWordDto>()
                    val created = contentWordService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateContentWordDto>()
                    contentWordService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(contentWordService.delete(id))
                }
            }

            // Words
            route("/words") {
                get { call.respond(wordService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = wordService.getById(id) ?: throw NotFoundException("Word not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateWordDto>()
                    val created = wordService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateWordDto>()
                    wordService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(wordService.delete(id))
                }
            }

            // Questions
            route("/questions") {
                get { call.respond(questionService.getAllQuestions()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = questionService.getQuestionById(id) ?: throw NotFoundException("Question not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateQuestionDto>()
                    val created = questionService.createQuestion(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateQuestionDto>()
                    questionService.updateQuestion(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(questionService.deleteQuestion(id))
                }
            }

            // Tests
            route("/tests") {
                get { call.respond(testService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = testService.getById(id) ?: throw NotFoundException("Test not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateTestDto>()
                    val created = testService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateTestDto>()
                    testService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(testService.delete(id))
                }
            }

            // TestExercises
            route("/test-exercises") {
                get { call.respond(testExerciseService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = testExerciseService.getById(id) ?: throw NotFoundException("TestExercise not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateTestExerciseDto>()
                    val created = testExerciseService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateTestExerciseDto>()
                    testExerciseService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(testExerciseService.delete(id))
                }
            }

            // ExercisesOnHold
            route("/exercises-on-hold") {
                get { call.respond(exerciseOnHoldService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = exerciseOnHoldService.getById(id) ?: throw NotFoundException("ExerciseOnHold not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateExerciseOnHoldDto>()
                    val created = exerciseOnHoldService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseOnHoldDto>()
                    exerciseOnHoldService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseOnHoldService.delete(id))
                }
            }

            // UnitsCompleted
            route("/units-completed") {
                get { call.respond(unitService.getAllUnitsCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = unitService.getUnitCompletedById(id) ?: throw NotFoundException("UnitCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateUnitCompletedDto>()
                    val created = unitService.createUnitCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateUnitCompletedDto>()
                    unitService.editUnitsCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(unitService.deleteUnitsCompleted(id))
                }
            }

            // ExercisesCompleted
            route("/exercises-completed") {
                get { call.respond(exerciseService.getAllExerciseCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = exerciseService.getExerciseCompletedById(id) ?: throw NotFoundException("ExerciseCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateExerciseCompletedDto>()
                    val created = exerciseService.createExerciseCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseCompletedDto>()
                    exerciseService.updateExerciseCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseService.deleteExerciseCompleted(id))
                }
            }

            // TestsCompleted
            route("/tests-completed") {
                get { call.respond(testService.getAllTestCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = testService.getTestCompletedById(id) ?: throw NotFoundException("TestCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateTestCompletedDto>()
                    val created = testService.createTestCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateTestCompletedDto>()
                    testService.updateTestCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(testService.deleteTestCompleted(id))
                }
            }

            // QuestionsCompleted
            route("/questions-completed") {
                get { call.respond(questionService.getAllQuestionsCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = questionService.getQuestionCompletedById(id) ?: throw NotFoundException("QuestionCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateQuestionCompletedDto>()
                    val created = questionService.createQuestionCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateQuestionCompletedDto>()
                    questionService.updateQuestionsCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(questionService.deleteQuestionsCompleted(id))
                }
            }

            // Notifications
            route("/notifications") {
                get { call.respond(notificationsService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val item = notificationsService.getById(id) ?: throw NotFoundException("Notification not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateNotificationDto>()
                    val created = notificationsService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateNotificationDto>()
                    notificationsService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                    call.respond(notificationsService.delete(id))
                }
            }

        }
    }
}