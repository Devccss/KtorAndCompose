package models

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime


// src/main/kotlin/models/Entities.kt

enum class Role { ADMIN, CONTENT_EDITOR, STUDENT }

object Users : IntIdTable() {
    val name = varchar("name", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100).nullable()
    val provider = varchar("provider", 100).nullable()
    val preferences = text("preferences").nullable()
    val activeNow = bool("active_now").default(false)
    val currentUnitId = integer("current_level_id").references(Units.id)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val role = enumerationByName<Role>("role", 20).default(Role.STUDENT)
}

enum class NotificationType { INFO, WARNING, ALERT }

object Notifications : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val title = varchar("title", 150)
    val notificationType =
        enumerationByName<NotificationType>("notification_type", 10).default(NotificationType.INFO)
    val message = text("message")
    val isRead = bool("is_read").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

enum class DifficultyLevel { A1, A2, B1, B2, C1, C2 }

object Units : IntIdTable() {
    val difficulty = enumerationByName<DifficultyLevel>("difficulty", 10)
    val name = varchar("name", 100)
    val description = text("description")
    val orderUnit = float("orderLevel").uniqueIndex()
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}


object Exercises : IntIdTable() {
    val levelId = integer("level_id").references(Units.id)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

enum class TypeTextExercise { normal, bold, italic, underline }

object ContentExercises : IntIdTable() {
    val nameExercise = varchar("name_exercise", 100)
    val typeText = enumerationByName<TypeTextExercise>("type_text", 10)
    val audioUrl = varchar("audio_url", 255).nullable()
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val exerciseId = integer("exercise_id").references(Exercises.id)

}

object ContentWords : IntIdTable() {
    val contentId = integer("content_id").references(ContentExercises.id)
    val wordId = integer("word_id").references(Words.id)
}

object Words : IntIdTable() {
    val english = varchar("english", 100)
    val spanish = varchar("spanish", 100)
    val phonetic = varchar("phonetic", 100).nullable()
    val description = text("description").nullable()
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

enum class TypeQuestion { ALTERNATIVE, OPEN }
object Questions : IntIdTable() {
    val questionText = text("question_text")
    val typeQuestion =
        enumerationByName<TypeQuestion>("type_question", 20).default(TypeQuestion.OPEN)
    val contentId = integer("content_id").references(ContentExercises.id).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}


enum class TestType { ALTERNATIVES, TRANSLATION, LISTENING, READING }

object Tests : IntIdTable() {
    val unitId = integer("unit_id").references(Units.id)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val testType = enumerationByName<TestType>("test_type", 20)
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object TestExercises : IntIdTable() {
    val testId = integer("test_id").references(Tests.id)
    val exerciseId = integer("exercise_id").references(Exercises.id)
}

object ExercisesOnHold : IntIdTable() {
    val exerciseId = integer("exercise_id").references(Exercises.id)
    val userId = integer("user_id").references(Users.id)
    val failureDate = datetime("failure_date").clientDefault { LocalDateTime.now() }
}

object UnitsCompleted : IntIdTable() {
    val unitId = integer("unit_id").references(Units.id)
    val userId = integer("user_id").references(Users.id)
    val completionDate = datetime("completion_date").clientDefault { LocalDateTime.now() }
}

object ExerciseCompleted : IntIdTable() {
    val exerciseId = integer("exercise_id").references(Exercises.id)
    val userId = integer("user_id").references(Users.id)
    val completionDate = datetime("completion_date").clientDefault { LocalDateTime.now() }
}

object TestCompleted : IntIdTable() {
    val testId = integer("test_id").references(Tests.id)
    val userId = integer("user_id").references(Users.id)
    val score = integer("score")
    val completionDate = datetime("completion_date").clientDefault { LocalDateTime.now() }
}

object QuestionCompleted : IntIdTable() {
    val questionId = integer("question_id").references(Questions.id)
    val userId = integer("user_id").references(Users.id)
    val openResponse = text("open_response").nullable()
    val completionDate = datetime("completion_date").clientDefault { LocalDateTime.now() }
}