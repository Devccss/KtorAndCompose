package config

import com.example.dtos.UnitDto
import com.example.dtos.UserDto
import io.github.cdimascio.dotenv.dotenv
import models.Alternatives
import models.ContentType
import models.ExerciseCompleted
import models.ExerciseContent
import models.Units
import models.UnitExerciseAssignments
import models.Users
import models.Exercises
import models.Notifications
import models.ExerciseWords
import models.Questions
import models.Role
import models.TestCompleted
import models.TestExercises
import models.Tests
import models.UnitsCompleted
import models.UserSessionLogs
import models.WelcomeTests
import models.Words
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

fun configureDatabase() {

    val dotenv = dotenv()
    val dbUrl = dotenv["DB_URL"]
    val dbUser = dotenv["DB_USER"]
    val dbPassword = dotenv["DB_PASSWORD"]

    Database.connect(
        url = dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword
    )

    transaction {

        //Eliminar todas las tablas antes de crearlo
        /*SchemaUtils.drop(
            Users,
            Units,
            Exercises,
            ExerciseContent,
            ExerciseCompleted,
            UnitExerciseAssignments,
            Notifications,
            Words,
            Questions,
            Tests,
            TestExercises,
            UnitsCompleted,
            TestCompleted,
            UserSessionLogs,
            ExerciseWords,
            Alternatives,
            WelcomeTests
        )*/

        SchemaUtils.create(
            Users,
            Units,
            Exercises,
            ExerciseContent,
            ExerciseCompleted,
            UnitExerciseAssignments,
            Notifications,
            Words,
            Questions,
            Tests,
            TestExercises,
            UnitsCompleted,
            TestCompleted,
            UserSessionLogs,
            ExerciseWords,
            Alternatives,
            WelcomeTests
        )
        arrayOf<Table>(UnitExerciseAssignments)
        createAdminUserIfNotExists()
    }
    createExercises()
}

fun createAdminUserIfNotExists() {
    val dotenv = dotenv()
    val adminName = dotenv["ADMIN_NAME"]
    val adminEmail = dotenv["ADMIN_EMAIL"]
    val adminPassword = dotenv["ADMIN_PASSWORD"]

    val student = dotenv["STUDENT_NAME"]
    val studentEmail = dotenv["STUDENT_EMAIL"]
    val studentPassword = dotenv["STUDENT_PASSWORD"]

    val editor = dotenv["EDITOR_NAME"]
    val editorEmail = dotenv["EDITOR_EMAIL"]
    val editorPassword = dotenv["EDITOR_PASSWORD"]

    val exists = Users.selectAll().where { Users.email eq adminEmail }.count() > 0
    if (!exists) {
        val hashed = BCrypt.hashpw(adminPassword, BCrypt.gensalt())
        val newId = Users.insert {
            it[email] = adminEmail
            it[password] = hashed
            it[name] = adminName
            it[preferences] = null
            it[provider] = "Created"
            it[currentUnitId] = null
            it[role] = Role.ADMIN
        }[Users.id]
        UserDto(
            id = newId.value,
            name = adminName,
            email = adminEmail,
            password = hashed,
            provider = "Created",
            preferences = null,
            activeNow = true,
            currentUnitId = null,
            createdAt = LocalDateTime.now().toString(),
            role = Role.ADMIN,
        )
    }
    val studentExists = Users.selectAll().where { Users.email eq studentEmail }.count() > 0
    if (!studentExists) {
        val hashed = BCrypt.hashpw(studentPassword, BCrypt.gensalt())
        val newId = Users.insert {
            it[email] = studentEmail
            it[password] = hashed
            it[name] = student
            it[preferences] = null
            it[provider] = "Created"
            it[currentUnitId] = null
            it[role] = Role.STUDENT
        }[Users.id]
        UserDto(
            id = newId.value,
            name = student,
            email = studentEmail,
            password = hashed,
            provider = "Created",
            preferences = null,
            activeNow = true,
            currentUnitId = null,
            createdAt = LocalDateTime.now().toString(),
            role = Role.STUDENT,
        )
    }

    val editorExists = Users.selectAll().where { Users.email eq editorEmail }.count() > 0
    if (!editorExists) {
        val hashed = BCrypt.hashpw(editorPassword, BCrypt.gensalt())
        val newId = Users.insert {
            it[email] = editorEmail
            it[password] = hashed
            it[name] = editor
            it[preferences] = null
            it[provider] = "Created"
            it[currentUnitId] = null
            it[role] = Role.CONTENT_EDITOR
        }[Users.id]
        UserDto(
            id = newId.value,
            name = editor,
            email = editorEmail,
            password = hashed,
            provider = "Created",
            preferences = null,
            activeNow = true,
            currentUnitId = null,
            createdAt = LocalDateTime.now().toString(),
            role = Role.CONTENT_EDITOR,
        )

        val new2 = Units.insert {
            it[difficulty] = models.DifficultyLevel.A1
            it[name] = "Unidad 1"
            it[description] = "Descripción de la unidad 1"
            it[orderUnit] = 1
            it[isActive] = true
        }[Units.id]
        UnitDto(
            id = new2.value,
            difficulty = models.DifficultyLevel.A1,
            name = "Unidad 1",
            description = "Descripción de la unidad 1",
            orderUnit = 1,
            isActive = true,
            createdAt = LocalDateTime.now().toString()
        )
    }
}

fun createExercises() {
    transaction {
        if (Exercises.selectAll().count() > 0L) {
            return@transaction
        }

        // Limpiar datos previos
        Alternatives.deleteAll()
        Questions.deleteAll()
        ExerciseWords.deleteAll()
        Words.deleteAll()
        ExerciseContent.deleteAll()
        Exercises.deleteAll()
        TestExercises.deleteAll()
        Tests.deleteAll()
        ExerciseCompleted.deleteAll()
        UnitsCompleted.deleteAll()
        TestCompleted.deleteAll()
        UserSessionLogs.deleteAll()

        // Crear unidades si no existen
        fun ensureUnit(
            orderUnit: Int,
            difficulty: models.DifficultyLevel,
            name: String,
            description: String,
        ): Int {
            val existing = Units.selectAll().where { Units.orderUnit eq orderUnit }.firstOrNull()
            return existing?.get(Units.id)?.value ?: Units.insert {
                it[this.difficulty] = difficulty
                it[this.name] = name
                it[this.description] = description
                it[this.orderUnit] = orderUnit
                it[this.isActive] = true
            }[Units.id].value
        }

        ensureUnit(
            orderUnit = 1,
            difficulty = models.DifficultyLevel.A1,
            name = "Unidad 1: To Be",
            description = "Introducción al verbo más importante del inglés"
        )

        // Insertar datos de seeds
        insertSeedData()
    }
}