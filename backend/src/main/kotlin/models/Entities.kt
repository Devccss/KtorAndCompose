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
    val provider = varchar("provider",100).nullable()
    val providerId = text("providerId")
    val preferences = text("preferences").nullable()
    val currentUnitId = integer("current_level_id").references(Units.id).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val role = enumerationByName<Role>("role", 20).default(Role.STUDENT)
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

object ContentExercise : IntIdTable() {
    val nameExercise = varchar("name_exercise", 100)
    val typeText = enumerationByName<TypeTextExercise>("type_text", 10)
    val audioUrl = varchar("audio_url", 255).nullable()
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val exerciseId = integer("exercise_id").references(Exercises.id)

}

object ExerciseWords : IntIdTable() {
    val contentId = integer("content_id").references(ContentExercise.id)
    val wordId = integer("word_id").references(Word.id)
}

object Word : IntIdTable() {
    val english = varchar("english", 100)
    val spanish = varchar("spanish", 100)
    val phonetic = varchar("phonetic", 100).nullable()
    val description = text("description").nullable()
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object Questions : IntIdTable() {
    val testId = integer("test_id").references(Tests.id)
    val questionText = text("question_text")
    val correctAnswer = text("correct_answer")
    val options = text("options") // JSON con opciones para preguntas de alternativas
    val orderLevel = integer("orderLevel")
    val contentId = integer("content_id").references(ContentExercise.id).nullable()
}


enum class TestType { ALTERNATIVES, TRANSLATION, LISTENING, READING }

object Tests : IntIdTable() {
    val levelId = integer("level_id").references(Units.id)
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


object CompleteUnits : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val unitsId = integer("level_id").references(Units.id)
    val completedAt = datetime("completed_at").clientDefault { LocalDateTime.now() }
}

object CompleteExercises : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val dialogId = integer("dialog_id").references(Exercises.id)
    val completedAt = datetime("completed_at").clientDefault { LocalDateTime.now() }
}

object AnsweredQuestions : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val questionId = integer("question_id").references(Questions.id)
    val isCorrect = bool("is_correct")
    val answeredAt = datetime("answered_at").clientDefault { LocalDateTime.now() }
}

object CompleteTests : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val testId = integer("test_id").references(Tests.id)
    val score = float("score")
    val completedAt = datetime("completed_at").clientDefault { LocalDateTime.now() }
}