package com.example.routes

import com.example.dtos.*
import com.example.services.*
import config.withRoles
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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
import models.ContentType
import models.DifficultyLevel
import models.NotificationCategory
import models.NotificationStatus
import models.NotificationSubCategory
import models.NotificationType
import models.Role
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.get
import services.UnitService


@Serializable
data class DatabaseTestResult(
    val status: String,
    val result: Int,
    val database: String
)

private fun resolveErrorMessage(cause: Exception, fallback: String): String {
    // Prioriza el mensaje que el desarrollador escribió al hacer throw.
    if (!cause.message.isNullOrBlank()) return cause.message!!

    var nested = cause.cause
    while (nested != null) {
        if (!nested.message.isNullOrBlank()) return nested.message!!
        nested = nested.cause
    }

    return fallback
}

fun Application.configureRouting() {

    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to resolveErrorMessage(cause, "Recurso no encontrado"))
            )
        }
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to resolveErrorMessage(cause, "Solicitud inválida"))
            )
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
    val notificationsService = get<NotificationsService>()
    val welcomeTestService = get<WelcomeTestService>()
    val aiQuestionGenerationService = get<AiQuestionGenerationService>()
    val userSessionLogsService = get<UserSessionLogsService>()
    val userStatisticsService = get<UserStatisticsService>()
    val unitExerciseAssignmentService = get<UnitExerciseAssignmentService>()

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
            authenticate("auth-jwt") {
                route("/session-logs") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {
                        post("/start") {
                            val dto = call.receive<CreateUserSessionLogDto>()
                            val created = userSessionLogsService.startSession(dto)
                            call.respond(HttpStatusCode.Created, created)
                        }
    
                        put("/{id}/close") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid session ID")
                            val dto = call.receive<CloseUserSessionLogDto>()
                            val closed = userSessionLogsService.closeSessionById(id, dto)
                            call.respond(closed)
                        }
    
                        put("/user/{userId}/close-open") {
                            val userId = call.parameters["userId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid user ID")
                            val dto = call.receive<CloseUserSessionLogDto>()
                            val closed = userSessionLogsService.closeOpenSessionByUserId(userId, dto)
                            call.respond(closed)
                        }
    
                        get("/{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid session ID")
                            val session = userSessionLogsService.getSessionById(id)
                                ?: throw NotFoundException("Session log not found")
                            call.respond(session)
                        }
    
                        get("/user/{userId}") {
                            val userId = call.parameters["userId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid user ID")
                            val sessions = userSessionLogsService.getSessionsByUserId(userId)
                            call.respond(sessions)
                        }
    
                        get("/user/{userId}/open") {
                            val userId = call.parameters["userId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid user ID")
                            val session = userSessionLogsService.getOpenSessionByUserId(userId)
                                ?: throw NotFoundException("No hay sesion abierta para este usuario")
                            call.respond(session)
                        }
    
                        get("/metrics/weekly/student") {
    
                            val fromDate = call.request.queryParameters["fromDate"]
                            val toDate = call.request.queryParameters["toDate"]
                            val metrics =
                                userSessionLogsService.getWeeklyMetrics(true, fromDate, toDate)
                            call.respond(metrics)
                        }

                    }
                    
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR){
                        get("/filter") {
                            val filters = call.receive<FilterUserSessionLogsDto>()
                            val sessions = userSessionLogsService.filter(filters)
                            call.respond(sessions)
                        }
    
                        get("/metrics/weekly") {
                            val fromDate = call.request.queryParameters["fromDate"]
                            val toDate = call.request.queryParameters["toDate"]
                            val metrics =
                                userSessionLogsService.getWeeklyMetrics(false, fromDate, toDate)
                            call.respond(metrics)
                        }
                        
                    }
                }
            }

            authenticate("auth-jwt"){
                withRoles(Role.ADMIN, Role.CONTENT_EDITOR) {
                    route("/ai/questions") {
                        post("generate/{contentId}") {
                            val contentId = call.parameters["contentId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid contentId")
                            val dto = call.receive<GenerateQuestionsFromAiRequestDto>()
                            val result = aiQuestionGenerationService.generateForContent(contentId, dto)
                            call.respond(HttpStatusCode.Created, result)
                        }
    
                        post("confirm/{contentId}") {
                            val contentId = call.parameters["contentId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid contentId")
                            val dto = call.receive<ConfirmAiQuestionRequestDto>()
                            val result = aiQuestionGenerationService.confirmForContent(contentId, dto)
                            call.respond(HttpStatusCode.Created, result)
                        }
                    }
                    
                }
            }

            
            route("/users") {
                // Public routes
                post("/login") {
                    val dto = call.receive<LoginDto>()
                    val user = userService.initSession(dto)
                    call.respond(user)
                }
                post("/register") {
                    val dto = call.receive<CreateUserDto>()
                    val user = userService.createUser(dto)
                    call.respond(HttpStatusCode.Created, user)
                }

                // User Statistics endpoints (no eliminar código existente, se agregan rutas nuevas)
                authenticate("auth-jwt") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {
                        route("/users") {
                            route("{userId}/stats") {
                                get {
                                    val userId = call.parameters["userId"]?.toIntOrNull()
                                        ?: throw BadRequestException("Invalid User ID")
                                    val stats = userStatisticsService.getUserStats(userId)
                                    call.respond(stats)
                                }
                            }

                            route("{userId}/completed-units") {
                                get {
                                    val userId = call.parameters["userId"]?.toIntOrNull()
                                        ?: throw BadRequestException("Invalid User ID")
                                    val units = unitService.getUnitsCompletedByUser(userId)
                                    call.respond(units)
                                }
                            }

                            route("{userId}/completed-exercises") {
                                get {
                                    val userId = call.parameters["userId"]?.toIntOrNull()
                                        ?: throw BadRequestException("Invalid User ID")
                                    val exercises = exerciseService.getExerciseCompletedByUser(userId)
                                    call.respond(exercises)
                                }
                            }

                            route("{userId}/completed-tests") {
                                get {
                                    val userId = call.parameters["userId"]?.toIntOrNull()
                                        ?: throw BadRequestException("Invalid User ID")
                                    val tests = testService.getTestsCompletedByUser(userId)
                                    call.respond(tests)
                                }
                            }

                            route("{userId}/failed-tests") {
                                get {
                                    val userId = call.parameters["userId"]?.toIntOrNull()
                                        ?: throw BadRequestException("Invalid User ID")
                                    val minScore = call.request.queryParameters["minScore"]?.toIntOrNull() ?: 60
                                    val tests = testService.getTestsFailedByUser(userId, minScore)
                                    call.respond(tests)
                                }
                            }

                            route("{userId}/weekly-hours") {
                                get {
                                    val userId = call.parameters["userId"]?.toIntOrNull()
                                        ?: throw BadRequestException("Invalid User ID")
                                    val result = userSessionLogsService.getWeeklyHoursByUserId(userId)
                                    call.respond(result)
                                }
                            }
                        }
                    }
                }
                authenticate("auth-jwt"){

                    // Protected routes
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR) {
                        get {
                            val users = userService.getAllUsers()
                            call.respond(users)
                        }
                        get("/filter") {
                            val name =
                                call.request.queryParameters["name"]?.takeIf { it.isNotBlank() }
                            val unitId = call.request.queryParameters["unitId"]?.toIntOrNull()
                            val role =
                                call.request.queryParameters["role"]?.let { Role.valueOf(it) }
                            val users = userService.getFilterUsers(
                                FilterUsersDto(
                                    name = name,
                                    unitId = unitId,
                                    role = role
                                )
                            )
                            call.respond(users)
                        }

                    }

                    get("{id}") {
                        val principal = call.principal<JWTPrincipal>()
                        val userIdPayload = principal?.payload?.getClaim("id")?.asInt()
                        val userRole = principal?.payload?.getClaim("role")?.asString()
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid ID in get user")
                        if((userIdPayload != id && userRole == Role.STUDENT.name) || userIdPayload == null){
                            call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "No tienes permiso para realizar esta acción")
                            )
                            return@get
                        }else{
                            val user = userService.getUserById(id)
                            if (user != null) {
                                if(user.role == Role.ADMIN && userIdPayload != id){
                                    call.respond(
                                        HttpStatusCode.Forbidden,
                                        mapOf("error" to "No tienes permiso para realizar esta acción")
                                    )
                                    return@get
                                }else{
                                    call.respond(user)
                                }
                            } else {
                                throw NotFoundException("User not found")
                            }
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
                    put("{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid ID in put user")
                        val dto = call.receive<UpdateUserDto>()
                        val updatedUser = userService.updateUser(id, dto)
                        call.respond(updatedUser)
                    }
                    withRoles(Role.ADMIN) {

                        delete("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID in delete user")
                            val success = userService.deleteUser(id)
                            call.respond(success)
                        }
                    }
                }


            }



            authenticate("auth-jwt"){
                // Units
                route("/units") {
                    withRoles( Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT){
                        
                        get("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID in get unit")
                            val item =
                                unitService.getUnitById(id) ?: throw NotFoundException("Unit not found")
                            call.respond(item)
                        }
                        get("/byTest/{testId}") {
                            val testId = call.parameters["testId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid Test ID")
                            val units = unitService.getUnitByTestId(testId)
                                ?: throw NotFoundException("No unit found for test ID $testId")
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
                        
                    }

                    route("{unitId}/assignments") {
                        withRoles(Role.STUDENT) {
                            get("/current") {
                                val unitId = call.parameters["unitId"]?.toIntOrNull()
                                    ?: throw BadRequestException("Invalid unit ID")
                                val mode = call.request.queryParameters["mode"] ?: "initial"
                                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5
                                val principal = call.principal<JWTPrincipal>()
                                val tokenUserId = principal?.payload?.getClaim("id")?.asInt()

                                if (tokenUserId == null ) {
                                    call.respond(
                                        HttpStatusCode.Forbidden,
                                        mapOf("error" to "No tienes permiso para solicitar asignaciones de otro usuario")
                                    )
                                    return@get
                                }

                                val assignment = unitExerciseAssignmentService.getCurrentAssignment(unitId, tokenUserId, mode, limit)
                                call.respond(assignment)
                            }

                            post {
                                val unitId = call.parameters["unitId"]?.toIntOrNull()
                                    ?: throw BadRequestException("Invalid unit ID")
                                val mode = call.request.queryParameters["mode"] ?: "initial"
                                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5
                                val principal = call.principal<JWTPrincipal>()
                                val tokenUserId = principal?.payload?.getClaim("id")?.asInt()

                                if (tokenUserId == null) {
                                    call.respond(
                                        HttpStatusCode.Forbidden,
                                        mapOf("error" to "No tienes permiso para solicitar asignaciones de otro usuario")
                                    )
                                    return@post
                                }

                                val assignment = unitExerciseAssignmentService.generateAssignment(unitId, tokenUserId, mode, limit)
                                call.respond(HttpStatusCode.Created, assignment)
                            }
                        }
                    }
                    withRoles(Role.CONTENT_EDITOR) {
                        get {
                            call.respond(unitService.getAllUnits())
                        }
                        post {
                            val dto = call.receive<CreateUnitDto>()
                            val created = unitService.createUnit(dto)
                            call.respond(HttpStatusCode.Created, created)
                        }
                        put("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID in put unit")
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
                                ?: throw BadRequestException("Invalid ID in delete unit")
                            call.respond(unitService.deleteUnit(id))
                        }

                    }
                }

                // Exercises
                route("/exercises") {
                    withRoles(Role.CONTENT_EDITOR){
                        get { call.respond(exerciseService.getAll()) }
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
                                val alternatives =
                                    questionService.getAlternativeByQuestionId(question.id)
                                alternatives.forEach { alt ->
                                    questionService.deleteAlternative(alt.id)
                                }
                                questionService.deleteQuestion(question.id)
                            }
                            exerciseContentService.deleteByExerciseId(id)
                            call.respond(exerciseService.delete(id))
                        }
                        
                    }
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {
                        get("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            val item =
                                exerciseService.getById(id)
                                    ?: throw NotFoundException("Exercise not found")
                            call.respond(item)
                        }
                        get("/search") {
                            val name = call.request.queryParameters["name"]
                            val unitId = call.request.queryParameters["unitId"]?.toIntOrNull()
                            val isActiveParam = call.request.queryParameters["isActive"]
                            val isActive = isActiveParam?.let {
                                when (it.lowercase()) {
                                    "true", "1", "yes" -> true
                                    "false", "0", "no" -> false
                                    else -> null
                                }
                            }
                            val filters =
                                FilterExercisesDto(name = name, isActive = isActive, unitId = unitId)
                            val results = exerciseService.searchExercises(filters)
                            call.respond(results)
                        }
                        get("unit/{unitId}") {
                            val unitId = call.parameters["unitId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid Unit ID")
                            val exercises = exerciseService.getByUnitId(unitId)
                            call.respond(exercises)
                        }
                        
                    }
                }

                route("/exerciseContent") {
                    withRoles(Role.ADMIN,Role.CONTENT_EDITOR, Role.STUDENT){
                        get("/search") {
                            val exerciseId = call.request.queryParameters["exerciseId"]?.toIntOrNull()
                            val contentType =
                                call.request.queryParameters["contentType"]?.let { ContentType.valueOf(it) }
                            val textContent = call.request.queryParameters["textContent"]
                            val grammarExplanation = call.request.queryParameters["grammarExplanation"]
                            val filters = FilterExerciseContentDto(
                                exerciseId = exerciseId,
                                contentType = contentType,
                                textContent = textContent,
                                grammarExplanation = grammarExplanation
                            )
                            call.respond(exerciseContentService.searchExerciseContent(filters))
                        }
                        get("/exercise/{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            val item = exerciseContentService.getByExerciseId(id)
                                ?: throw NotFoundException("ExerciseContent not found")
                            call.respond(item)
                        }
                        
                    }
                    withRoles(Role.CONTENT_EDITOR){
                        get { call.respond(exerciseContentService.getAllExerciseContent()) }
                        post("/{id}") {
                            val dto = call.receive<CreateExerciseContentDto>()
                            val exerciseId = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid Exercise ID")
                            val created = exerciseContentService.createExerciseContent(exerciseId, dto)
                            call.respond(HttpStatusCode.Created, created)
                        }
                        put("/exercise/{id}") {
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
                }

                // QuestionExercises
                route("/question-exercises") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT){
                        get { call.respond(exerciseWordService.getAll()) }
                        get("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            val item = exerciseWordService.getById(id)
                                ?: throw NotFoundException("ContentExercise not found")
                            call.respond(item)
                        }
                        
                    }
                    withRoles(Role.CONTENT_EDITOR){
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
                }

                // ExerciseWords
                
                route("/exerciseWords") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT){
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
                        
                    }
                    withRoles(Role.CONTENT_EDITOR){
                        get { call.respond(exerciseWordService.getAll()) }
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
                }

                // Words
                route("/words") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT){
                        get("/search") {
                            val exerciseId = call.request.queryParameters["exerciseId"]?.toIntOrNull()
                            val english = call.request.queryParameters["english"]
                            val spanish = call.request.queryParameters["spanish"]
                            val phonetic = call.request.queryParameters["phonetic"]
                            val isActiveParam = call.request.queryParameters["isActive"]
                            val isActive = isActiveParam?.let {
                                when (it.lowercase()) {
                                    "true", "1", "yes" -> true
                                    "false", "0", "no" -> false
                                    else -> null
                                }
                            }
                            val filters = FilterWordsDto(
                                exerciseId = exerciseId,
                                english = english,
                                spanish = spanish,
                                phonetic = phonetic,
                                isActive = isActive
                            )
                            call.respond(wordService.searchWords(filters))
                        }
                        get("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            val item = wordService.getById(id) ?: throw NotFoundException("Word not found")
                            call.respond(item)
                        }
                        
                    }
                    withRoles(Role.CONTENT_EDITOR){
                        get { call.respond(wordService.getAll()) }
                        post {
                            val dto = call.receive<CreateWordDto>()
                            val created = wordService.create(dto)
                            call.respond(HttpStatusCode.Created, created)
                        }
                        put("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            val dto = call.receive<UpdateWordDto>()
                            val success = wordService.update(id, dto)
                            call.respond(success)
                        }
                        delete("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            call.respond(wordService.delete(id))
                        }
                        
                    }
                }

                // Questions
                route("/questions") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {
                        get { call.respond(questionService.getAllQuestions()) }
                        get("/search") {
                            val exerciseContentId =
                                call.request.queryParameters["exerciseContentId"]?.toIntOrNull()
                            val questionText = call.request.queryParameters["questionText"]
                            val isActiveParam = call.request.queryParameters["isActive"]
                            val isActive = isActiveParam?.let {
                                when (it.lowercase()) {
                                    "true", "1", "yes" -> true
                                    "false", "0", "no" -> false
                                    else -> null
                                }
                            }
                            val filters = FilterQuestionsDto(
                                exerciseContentId = exerciseContentId,
                                questionText = questionText,
                                isActive = isActive
                            )
                            call.respond(questionService.searchQuestions(filters))
                        }
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
                    }
                    withRoles(Role.CONTENT_EDITOR) {
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
                }

                route("/alternatives") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {
                        get { call.respond(questionService.getAllAlternatives()) }
                        get("question/{questionId}") {
                            val questionId = call.parameters["questionId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid Question ID")
                            val alternatives = questionService.getAlternativeByQuestionId(questionId)
                            call.respond(alternatives)
                        }
                    }
                    withRoles(Role.CONTENT_EDITOR) {
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
                }

                // Tests
                route("/tests") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {

                        get("/search") {
                            val name = call.request.queryParameters["name"]
                            val unitId = call.request.queryParameters["unitId"]?.toIntOrNull()
                            val isActiveParam = call.request.queryParameters["isActive"]
                            val isActive = isActiveParam?.let {
                                when (it.lowercase()) {
                                    "true", "1", "yes" -> true
                                    "false", "0", "no" -> false
                                    else -> null
                                }
                            }
                            call.respond(
                                testService.filtered(
                                    FilterTestsDto(
                                        name = name,
                                        unitId = unitId,
                                        isActive = isActive
                                    )
                                )
                            )
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
                    }
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR) {
                        // Obtener estado de repaso para una unidad/test y usuario
                        get("/review-status") {
                            val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid userId")
                            val unitId = call.request.queryParameters["unitId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid unitId")
                            val testId = call.request.queryParameters["testId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid testId")

                            val status = testService.getUnitReviewStatus(userId, unitId, testId)
                            call.respond(status)
                        }
                    }
                    withRoles(Role.CONTENT_EDITOR) {
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
                        get("/byExercise/{exerciseId}") {
                            val exerciseId = call.parameters["exerciseId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid Exercise ID")
                            val items = testExerciseService.searchByIds(exerciseId)
                            val test = testService.getById(items.first().testId) ?: throw NotFoundException(
                                "No test found for exercise ID $exerciseId"
                            )
                            call.respond(test)
                        }
                        get("/byUnit/{unitId}") {
                            val unitId = call.parameters["unitId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid Unit ID")
                            val tests = testService.getTestsByUnitId(unitId)
                                ?: throw NotFoundException("No tests found for unit ID $unitId")
                            call.respond(tests)
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
                        delete("{testId}") {
                            val id = call.parameters["testId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            testExerciseService.deleteByTestId(id)
                            call.respond(testService.delete(id))
                        }
                    }
                }

                // TestExercises
                route("/testExercises") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {
                        get { call.respond(testExerciseService.getAll()) }
                        get("{id}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            val item =
                                testExerciseService.getById(id)
                                    ?: throw NotFoundException("TestExercise not found")
                            call.respond(item)
                        }

                        get("/search") {
                            val testId = call.queryParameters["testId"]?.toIntOrNull()
                            val exerciseId = call.queryParameters["exerciseId"]?.toIntOrNull()

                            if (testId == null && exerciseId == null) {
                                throw BadRequestException("Alguno de los ids (testId o exerciseId) debe ser proporcionado")
                            }
                            val item = testExerciseService.searchByIds(testId, exerciseId)
                            call.respond(item)
                        }
                    }
                    withRoles(Role.CONTENT_EDITOR) {
                        post {
                            val dto = call.receive<CreateTestExerciseDto>()
                            //if (test?.unitId != exercise?.unitId && !isWelcome) {
                            //  throw BadRequestException("El test y el ejercicio deben pertenecer a la misma unidad")
                            //}
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
                            call.respond(testExerciseService.deleteByExerciseId(id))
                        }
                    }
                }

                // WelcomeTest
                route("/welcomeTest") {
                    withRoles(Role.ADMIN, Role.CONTENT_EDITOR, Role.STUDENT) {
                        get {
                            val allWelcomeTests = welcomeTestService.getAll()
                            call.respond(allWelcomeTests)
                        }
                        get("/tests") {
                            val allWelcomeTests = welcomeTestService.getAll()
                            val tests = allWelcomeTests.mapNotNull { welcome ->
                                testService.getById(welcome.testId)
                            }
                            call.respond(tests)
                        }
                        get("{TestId}") {
                            val id = call.parameters["TestId"]?.toIntOrNull()
                                ?: throw BadRequestException("Invalid ID")
                            val item = welcomeTestService.getByTestId(id)
                                ?: throw NotFoundException("WelcomeTest not found")

                            call.respond(
                                testService.getById(item.testId)
                                    ?: throw NotFoundException("Test not found for WelcomeTest with testId $id")
                            )
                        }
                    }
                    withRoles(Role.CONTENT_EDITOR) {
                        post {
                            val dto = call.receive<CreateWelcomeTestDto>()
                            val created = welcomeTestService.createWelcomeTest(dto)
                            testService.getById(created.testId)?.let {
                                call.respond(HttpStatusCode.Created, it)
                            }
                                ?: throw NotFoundException("Test not found for created WelcomeTest with testId ${created.testId}")
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
                }

                // UnitsCompleted
                route("/unitsCompleted") {
                    get { call.respond(unitService.getAllUnitsCompleted()) }

                    get("{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid ID")
                        val item = unitService.getUnitCompletedById(id)
                            ?: throw NotFoundException("UnitCompleted not found")
                        call.respond(item)
                    }
                    get("user/{userId}") {
                        val userId = call.parameters["userId"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid User ID")
                        val items = unitService.getUnitsCompletedByUser(userId)
                        call.respond(items)
                    }
                    post {
                        val dto = call.receive<CreateUnitCompletedDto>()
                        val created = unitService.createUnitCompleted(dto)

                        val totalUnits = unitService.searchUnits(FilterUnitsDto(isActive = true)).size
                        val completedUnits = unitService.getUnitsCompletedByUser(dto.userId)
                            .map { it.id }
                            .toSet()
                            .size
                        val remainingUnits = (totalUnits - completedUnits).coerceAtLeast(0)

                        notificationsService.notifyIfUserIsCloseToFinishUnits(
                            dto.userId,
                            remainingUnits
                        )

                        val unit = unitService.getUnitById(created.unitId)


                        call.respond(
                            HttpStatusCode.Created,
                            unit
                                ?: throw NotFoundException("Error al completar la unidad con ID ${created.unitId}")
                        )
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
                route("/exercisesCompleted") {
                    get { call.respond(exerciseService.getAllExerciseCompleted()) }
                    get("{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid ID")
                        val item = exerciseService.getExerciseCompletedById(id)
                            ?: throw NotFoundException("ExerciseCompleted not found")
                        call.respond(item)
                    }
                    get("user/{userId}") {
                        val userId = call.parameters["userId"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid User ID")
                        val items = exerciseService.getExerciseCompletedByUser(userId)
                        call.respond(items)
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
                route("/testsCompleted") {
                    get { call.respond(testService.getAllTestCompleted()) }
                    get("{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid ID")
                        val item = testService.getTestCompletedById(id)
                            ?: throw NotFoundException("TestCompleted not found")
                        call.respond(item)
                    }
                    get("user/{userId}") {
                        val userId = call.parameters["userId"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid User ID")
                        val items = testService.getTestsCompletedByUser(userId)
                        call.respond(items)
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
                    get("/search") {
                        val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                        val title = call.request.queryParameters["title"]
                        val notificationType = call.request.queryParameters["notificationType"]?.let {
                            NotificationType.valueOf(it)
                        }
                        val category = call.request.queryParameters["category"]?.let {
                            NotificationCategory.valueOf(it)
                        }
                        val subCategory = call.request.queryParameters["subCategory"]?.let {
                            NotificationSubCategory.valueOf(it)
                        }
                        val status =
                            call.request.queryParameters["status"]?.let { NotificationStatus.valueOf(it) }
                        val filters = FilterNotificationsDto(
                            userId = userId,
                            title = title,
                            notificationType = notificationType,
                            category = category,
                            subCategory = subCategory,
                            status = status
                        )
                        call.respond(notificationsService.searchNotifications(filters))
                    }
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
                    put("{id}/read") {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid ID")
                        val updated = notificationsService.markAsRead(id)
                        call.respond(updated)
                    }
                    put("{id}/unread") {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("Invalid ID")
                        val updated = notificationsService.markAsUnread(id)
                        call.respond(updated)
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
}
