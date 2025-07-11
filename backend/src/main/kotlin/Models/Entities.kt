
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime


// src/main/kotlin/models/Entities.kt

object Users : IntIdTable() {
    val name = varchar("name", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val preferences = text("preferences").nullable() // JSON con preferencias
    val currentLevelId = integer("current_level_id").references(Levels.id).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val role = enumerationByName<Role>("role", 20).default(Role.STUDENT)
}

enum class Role { ADMIN, TEACHER, STUDENT }

object Levels : IntIdTable() {
    val accent = integer("accent") // 1,2,3...
    val difficulty = enumerationByName<DifficultyLevel>("difficulty", 10)
    val name = varchar("name", 100)
    val description = text("description")
    val orderLevel = float("orderLevel").uniqueIndex()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

enum class DifficultyLevel { A1, A2, B1, B2, C1, C2 }

object Dialogs : IntIdTable() {
    val levelId = integer("level_id").references(Levels.id)
    val name = varchar("name", 100)
    val difficulty = enumerationByName<DifficultyLevel>("difficulty", 10)
    val description = text("description").nullable()
    val audioUrl = varchar("audio_url", 255).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object Phrases : IntIdTable() {
    val dialogId = integer("dialog_id").references(Dialogs.id)
    val englishText = text("english_text")
    val spanishText = text("spanish_text")
    val orderLevel = integer("orderLevel")
    val isActive = bool("is_active").default(true)
}

object Words : IntIdTable() {
    val english = varchar("english", 100)
    val spanish = varchar("spanish", 100)
    val phonetic = varchar("phonetic", 100).nullable()
    val description = text("description").nullable()
    val difficulty = enumerationByName<DifficultyLevel>("difficulty", 10).nullable()
    val isActive = bool("is_active").default(true)
}

object PhraseWords : org.jetbrains.exposed.v1.core.Table("phrase_words") {
    internal val phraseId = integer("phrase_id").references(Phrases.id)
    internal val wordId = integer("word_id").references(Words.id)
    override val primaryKey = PrimaryKey(phraseId, wordId)
}

object UserPhraseStandby : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val phraseId = integer("phrase_id").references(Phrases.id)
    val incorrectAttempts = integer("incorrect_attempts").default(0)
    val addedAt = datetime("added_at").clientDefault { LocalDateTime.now() }
}

object Tests : IntIdTable() {
    val levelId = integer("level_id").references(Levels.id)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val testType = enumerationByName<TestType>("test_type", 20)
    val difficulty = enumerationByName<DifficultyLevel>("difficulty", 10)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

enum class TestType { ALTERNATIVES, TRANSLATION, LISTENING, READING }

object TestQuestions : IntIdTable() {
    val testId = integer("test_id").references(Tests.id)
    val questionText = text("question_text")
    val correctAnswer = text("correct_answer")
    val options = text("options") // JSON con opciones para preguntas de alternativas
    val orderLevel = integer("orderLevel")
}

object UserProgress : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val levelId = integer("level_id").references(Levels.id)
    val completedDialogs = integer("completed_dialogs").default(0)
    val totalDialogs = integer("total_dialogs")
    val testScore = integer("test_score").nullable()
    val isLevelCompleted = bool("is_level_completed").default(false)
    val lastAccessed = datetime("last_accessed").nullable()
}