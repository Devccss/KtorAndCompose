package com.example.routes

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
import models.DifficultyLevel
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
    val exerciseContentService = get<ExerciseContentService>()
    val exerciseWordService = get<ExerciseWordService>()
    val wordService = get<WordService>()
    val questionService = get<QuestionService>()
    val testService = get<TestService>()
    val testExerciseService = get<TestExerciseService>()
    val exerciseOnHoldService = get<ExerciseOnHoldService>()
    val notificationsService = get<NotificationsService>()
    val welcomeTestService = get<WelcomeTestService>()

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
                get("name/{name}") {
                    val name = call.parameters["name"]
                        ?: throw BadRequestException("Name parameter is required")
                    val users = userService.getUsersByName(name)
                    call.respond(users)
                }
                post("filter") {
                    val filters = call.receive<FilterUsersDto>()
                    val users = userService.getFilterUsers(filters)
                    call.respond(users)
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
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item =
                        unitService.getUnitById(id) ?: throw NotFoundException("Unit not found")
                    call.respond(item)
                }
                get("/byTest/{testId}") {
                    val testId = call.parameters["testId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Test ID")
                    val units = unitService.getUnitByTestId(testId)?: throw NotFoundException("No unit found for test ID $testId")
                    call.respond(units)
                }
                get("/search") {
                    val name = call.request.queryParameters["name"]
                    val difficulty =
                        call.request.queryParameters["difficulty"]?.let { DifficultyLevel.valueOf(it) }
                    val isActiveParam = call.request.queryParameters["isActive"]
                    val isActive = isActiveParam?.let {
                        when (it.lowercase()) {
                            "true", "1", "yes" -> true
                            "false", "0", "no" -> false
                            else -> null
                        }
                    }
                    val filters =
                        FilterUnitsDto(name = name, difficulty = difficulty, isActive = isActive)
                    val results = unitService.searchUnits(filters)
                    call.respond(results)
                }
                post {
                    val dto = call.receive<CreateUnitDto>()
                    val created = unitService.createUnit(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateUnitDto>()
                    unitService.updateUnit(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                put("reorder") {
                    val dto = call.receive<List<Pair<Int, Int>>>()
                    print("Received reorder request: $dto")
                    val success = unitService.reorderUnits(dto)
                    call.respond(success)
                }

                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(unitService.deleteUnit(id))
                }
            }

            // Exercises
            route("/exercises") {
                get { call.respond(exerciseService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item =
                        exerciseService.getById(id) ?: throw NotFoundException("Exercise not found")
                    call.respond(item)
                }
                get("/search") {
                    val name = call.request.queryParameters["name"]
                    val isActiveParam = call.request.queryParameters["isActive"]
                    val isActive = isActiveParam?.let {
                        when (it.lowercase()) {
                            "true", "1", "yes" -> true
                            "false", "0", "no" -> false
                            else -> null
                        }
                    }
                    val filters = FilterExercisesDto(name = name, isActive = isActive)
                    val results = exerciseService.searchExercises(filters)
                    call.respond(results)
                }
                get("unit/{unitId}") {
                    val unitId = call.parameters["unitId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Unit ID")
                    val exercises = exerciseService.getByUnitId(unitId)
                    call.respond(exercises)
                }
                post {
                    val dto = call.receive<CreateExerciseDto>()
                    val created = exerciseService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseDto>()
                    val success = exerciseService.update(id, dto)
                    call.respond(success)
                }
                put("reorder") {
                    val dto = call.receive<List<Pair<Int, Int>>>()
                    print("Received reorder request: $dto")
                    val success = exerciseService.reorderExercises(dto)
                    call.respond(success)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val questions = questionService.getQuestionsByExerciseId(id)
                    questions.forEach { question ->
                        val alternatives = questionService.getAlternativeByQuestionId(question.id)
                        alternatives.forEach { alt ->
                            questionService.deleteAlternative(alt.id)
                        }
                        questionService.deleteQuestion(question.id)
                    }
                    exerciseContentService.deleteByExerciseId(id)
                    call.respond(exerciseService.delete(id))
                }
            }

            route("/exerciseContent"){
                get { call.respond(exerciseContentService.getAllExerciseContent()) }
                get("/exercise/{id}"){
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = exerciseContentService.getByExerciseId(id)
                        ?: throw NotFoundException("ExerciseContent not found")
                    call.respond(item)
                }
                post("/{id}"){
                    val dto = call.receive<CreateExerciseContentDto>()
                    val exerciseId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Exercise ID")
                    val created = exerciseContentService.createExerciseContent(exerciseId, dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("/exercise/{id}"){
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseContentDto>()
                    val success = exerciseContentService.updateContentByExerciseId(id, dto)
                    call.respond(success)
                }
                delete("/exercise/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseContentService.deleteByExerciseId(id))
                }
            }

            // QuestionExercises
            route("/question-exercises") {
                get { call.respond(exerciseWordService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = exerciseWordService.getById(id)
                        ?: throw NotFoundException("ContentExercise not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateExerciseWordDto>()
                    val created = exerciseWordService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseWordDto>()
                    exerciseWordService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseWordService.delete(id))
                }
            }

            // ExerciseWords
            route("/exerciseWords") {
                get { call.respond(exerciseWordService.getAll()) }
                get("/exercise/{id}") {
                    val exerciseId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Exercise ID")
                    val items = exerciseWordService.getByExerciseId(exerciseId)
                    call.respond(items)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = exerciseWordService.getById(id)
                        ?: throw NotFoundException("ContentWord not found")
                    call.respond(item)
                }
                post {

                    val dto = call.receive<CreateExerciseWordDto>()
                    val exercise = exerciseService.getById(dto.exerciseId)
                    val word = wordService.getById(dto.wordId)
                    if (exercise == null) {
                        throw BadRequestException("Exercise with ID ${dto.exerciseId} does not exist.")
                    }
                    if (word == null) {
                        throw BadRequestException("Word with ID ${dto.wordId} does not exist.")
                    }
                    val created = exerciseWordService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseWordDto>()
                    val exercise = dto.exerciseId?.let { exerciseService.getById(it) }
                    val word = dto.wordId?.let { wordService.getById(it) }
                    if (exercise == null) {
                        throw BadRequestException("Question with ID ${dto.exerciseId} does not exist.")
                    }
                    if (word == null) {
                        throw BadRequestException("Word with ID ${dto.wordId} does not exist.")
                    }
                    exerciseWordService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseWordService.delete(id))
                }
            }

            // Words
            route("/words") {
                get { call.respond(wordService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = wordService.getById(id) ?: throw NotFoundException("Word not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateWordDto>()
                    val created = wordService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateWordDto>()
                    val success= wordService.update(id, dto)
                    call.respond(success)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(wordService.delete(id))
                }
            }

            // Questions
            route("/questions") {
                get { call.respond(questionService.getAllQuestions()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = questionService.getQuestionById(id)
                        ?: throw NotFoundException("Question not found")
                    call.respond(item)
                }
                get("exercise/{contentId}") {
                    val contentId = call.parameters["contentId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Exercise ID")
                    val questions = questionService.getQuestionsByExerciseId(contentId)
                    call.respond(questions)
                }
                get("alternatives/{questionId}") {
                    val questionId = call.parameters["questionId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Question ID")
                    val alternatives = questionService.getAlternativeByQuestionId(questionId)
                    call.respond(alternatives)
                }
                post("{contentId}") {
                    val contentId = call.parameters["contentId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Exercise ID")
                    val dto = call.receive<CreateQuestionDto>()
                    val created = questionService.createQuestion(contentId, dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateQuestionDto>()
                    val success = questionService.updateQuestion(id, dto)
                    call.respond(success)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(questionService.deleteQuestion(id))
                }
            }

            route("/alternatives") {
                get { call.respond(questionService.getAllAlternatives()) }
                get("question/{questionId}") {
                    val questionId = call.parameters["questionId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Question ID")
                    val alternatives = questionService.getAlternativeByQuestionId(questionId)
                    call.respond(alternatives)
                }
                post("{id}") {
                    val questionId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Question ID")
                    val dto = call.receive<CreateAlternativeDto>()
                    val created = questionService.createAlternative(questionId, dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateAlternativeDto>()
                    questionService.updateAlternative(id, dto)
                    call.respond(true)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(questionService.deleteAlternative(id))
                }
            }

            // Tests
            route("/tests") {
                get {

                    val tests = testService.getAll().filter { test ->
                        welcomeTestService.getByTestId(test.id) == null
                    }
                    call.respond(tests)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = testService.getById(id) ?: throw NotFoundException("Test not found")
                    call.respond(item)
                }
                get("/exercises/{testId}") {
                    val testId = call.parameters["testId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Test ID")
                    val items = testExerciseService.searchByIds(testId = testId)
                    val allExercises = exerciseService.getAll()
                    val exercisesInTest = items.mapNotNull { item ->
                        allExercises.find { it.id == item.exerciseId }
                    }
                    call.respond(exercisesInTest)
                }
                get("/byUnit/{unitId}") {
                    val unitId = call.parameters["unitId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Unit ID")
                    val tests = testService.getTestsByUnitId(unitId)?: throw NotFoundException("No tests found for unit ID $unitId")
                    call.respond(tests)
                }
                get("/byExercise/{exerciseId}") {
                    val exerciseId = call.parameters["exerciseId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid Exercise ID")
                    val items = testExerciseService.searchByIds(exerciseId)
                    val test = testService.getById( items.first().testId)?: throw NotFoundException("No test found for exercise ID $exerciseId")
                    call.respond(test)
                }
                post {
                    val dto = call.receive<CreateTestDto>()

                    val existeTestNoWelcomeEnUnidad = testService.getAll().any { test ->
                        test.unitId == dto.unitId && welcomeTestService.getByTestId(test.id) == null
                    }

                    if (existeTestNoWelcomeEnUnidad) {
                        val unit = unitService.getUnitById(dto.unitId)
                        throw BadRequestException("Ya existe un test para la unidad: ${unit?.name}")
                    }

                    val created = testService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }

                post("/welcome") {
                    val dto = call.receive<CreateTestDto>()
                    val created = testService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateTestDto>()
                    testService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val testExercise = testExerciseService.searchByIds(testId = id)
                    testExercise.forEach { item ->
                        testExerciseService.delete(item.id)
                    }

                    call.respond(testService.delete(id))
                }
            }

            // TestExercises
            route("/testExercises") {
                get { call.respond(testExerciseService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item =
                        testExerciseService.getById(id) ?: throw NotFoundException("TestExercise not found")
                    call.respond(item)
                }

                get("/search") {
                    val testId = call.queryParameters["testId"]?.toIntOrNull()
                    val exerciseId = call.queryParameters["exerciseId"]?.toIntOrNull()

                    if (testId == null && exerciseId == null) {
                        throw BadRequestException("Alguno de los ids (testId o exerciseId) debe ser proporcionado")
                    }
                    val item = testExerciseService.searchByIds( testId, exerciseId)
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateTestExerciseDto>()
                    val test = testService.getById(dto.testId)
                    val exercise = exerciseService.getById(dto.exerciseId)
                    if (test?.unitId != exercise?.unitId){
                        throw BadRequestException("El test y el ejercicio deben pertenecer a la misma unidad")
                    }
                    val created = testExerciseService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateTestExerciseDto>()
                    testExerciseService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{exerciseId}") {
                    val id = call.parameters["exerciseId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(testExerciseService.delete(id))
                }
            }

            route("/welcomeTest"){
                get {
                    val allWelcomeTests = welcomeTestService.getAll()
                    call.respond(allWelcomeTests)
                }
                get("/tests") {
                    val allWelcomeTests = welcomeTestService.getAll()
                    val tests = allWelcomeTests.mapNotNull {welcome ->
                        testService.getById(welcome.testId)
                    }
                    call.respond(tests)
                }
                get("{TestId}") {
                    val id = call.parameters["TestId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = welcomeTestService.getByTestId(id) ?: throw NotFoundException("WelcomeTest not found")

                    call.respond(testService.getById(item.testId) ?: throw NotFoundException("Test not found for WelcomeTest with testId $id"))
                }
                post {
                    val dto = call.receive<CreateWelcomeTestDto>()
                    val created = welcomeTestService.createWelcomeTest(dto)
                    testService.getById(created.testId)?.let {
                        call.respond(HttpStatusCode.Created, it)
                    } ?: throw NotFoundException("Test not found for created WelcomeTest with testId ${created.testId}")
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateWelcomeTestDto>()
                    val update = welcomeTestService.updateWelcomeTest(id, dto)
                    call.respond(update)
                }
                delete("{TestId}") {
                    val id = call.parameters["TestId"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(welcomeTestService.deleteByTestId(id))
                }
            }

            // ExercisesOnHold
            route("/exercises-on-hold") {
                get { call.respond(exerciseOnHoldService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = exerciseOnHoldService.getById(id)
                        ?: throw NotFoundException("ExerciseOnHold not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateExerciseOnHoldDto>()
                    val created = exerciseOnHoldService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseOnHoldDto>()
                    exerciseOnHoldService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseOnHoldService.delete(id))
                }
            }

            // UnitsCompleted
            route("/units-completed") {
                get { call.respond(unitService.getAllUnitsCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = unitService.getUnitCompletedById(id)
                        ?: throw NotFoundException("UnitCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateUnitCompletedDto>()
                    val created = unitService.createUnitCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateUnitCompletedDto>()
                    unitService.editUnitsCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(unitService.deleteUnitsCompleted(id))
                }
            }

            // ExercisesCompleted
            route("/exercises-completed") {
                get { call.respond(exerciseService.getAllExerciseCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = exerciseService.getExerciseCompletedById(id)
                        ?: throw NotFoundException("ExerciseCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateExerciseCompletedDto>()
                    val created = exerciseService.createExerciseCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateExerciseCompletedDto>()
                    exerciseService.updateExerciseCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(exerciseService.deleteExerciseCompleted(id))
                }
            }

            // TestsCompleted
            route("/tests-completed") {
                get { call.respond(testService.getAllTestCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = testService.getTestCompletedById(id)
                        ?: throw NotFoundException("TestCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateTestCompletedDto>()
                    val created = testService.createTestCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateTestCompletedDto>()
                    testService.updateTestCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(testService.deleteTestCompleted(id))
                }
            }

            // QuestionsCompleted
            route("/questions-completed") {
                get { call.respond(questionService.getAllQuestionsCompleted()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = questionService.getQuestionCompletedById(id)
                        ?: throw NotFoundException("QuestionCompleted not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateQuestionCompletedDto>()
                    val created = questionService.createQuestionCompleted(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateQuestionCompletedDto>()
                    questionService.updateQuestionsCompleted(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(questionService.deleteQuestionsCompleted(id))
                }
            }

            // Notifications
            route("/notifications") {
                get { call.respond(notificationsService.getAll()) }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val item = notificationsService.getById(id)
                        ?: throw NotFoundException("Notification not found")
                    call.respond(item)
                }
                post {
                    val dto = call.receive<CreateNotificationDto>()
                    val created = notificationsService.create(dto)
                    call.respond(HttpStatusCode.Created, created)
                }
                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    val dto = call.receive<UpdateNotificationDto>()
                    notificationsService.update(id, dto)
                    call.respond(HttpStatusCode.OK)
                }
                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID")
                    call.respond(notificationsService.delete(id))
                }
            }

        }
    }
}