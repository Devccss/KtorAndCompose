package config

import com.example.dtos.UserDto
import io.github.cdimascio.dotenv.dotenv
import models.Alternatives
import models.ExerciseCompleted
import models.ExerciseContent
import models.Units
import models.Users
import models.Exercises
import models.ExercisesOnHold
import models.Notifications
import models.QuestionWords
import models.Questions
import models.Role
import models.TestCompleted
import models.TestExercises
import models.Tests
import models.UnitsCompleted
import models.Words
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

fun configureDatabases() {

    val dotenv = dotenv()
    val dbUrl = dotenv["DB_URL"]
    val dbUser = dotenv["DB_USER"]
    val dbPassword = dotenv["DB_PASSWORD"]

    Database.connect(
        url = dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword
    )

    transaction {
        SchemaUtils.create(
            Users, Units, Exercises,ExerciseContent,ExercisesOnHold,ExerciseCompleted, Notifications, Words, Questions,
            Tests, TestExercises, UnitsCompleted, TestCompleted, QuestionWords, Alternatives
        )
        createAdminUserIfNotExists()
    }
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

    val exists = Users.selectAll().where{ Users.email eq adminEmail }.count() > 0
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
    }
}