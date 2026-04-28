package config

import com.example.dtos.UserDto
import io.github.cdimascio.dotenv.dotenv
import models.Alternatives
import models.ContentType
import models.ExerciseCompleted
import models.ExerciseContent
import models.Units
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
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.deleteAll
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
            Users,
            Units,
            Exercises,
            ExerciseContent,
            ExerciseCompleted,
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
    }
}

fun createExercises() {
    transaction {

        if (Exercises.selectAll().count() > 0L) {
            return@transaction
        }
        Alternatives.deleteAll()
        Questions.deleteAll()
        ExerciseWords.deleteAll()
        Words.deleteAll()
        ExerciseContent.deleteAll()
        Exercises.deleteAll()


        data class QSeed(val text: String, val alts: List<String>, val correct: String)
        data class VSeed(val english: String, val spanish: String, val phonetic: String)
        data class ExSeed(
            val name: String,
            val unitId: Int,
            val desc: String,
            val content: String,
            val grammar: String,
            val qs: List<QSeed>,
            val vocab: List<VSeed>
        )

        val seeds = listOf(
            ExSeed(
                "Verbo To Be básico",
                1,
                "Uso básico del verbo to be con pronombres",
                "Maria is a nurse. Luis and I are Mexican.",
                "El verbo to be se utiliza para expresar identidad o estado. En presente simple se conjuga como am, is o are dependiendo del pronombre personal. El verbo to be cambia según el sujeto. Cuando el sujeto es plural como \"Luis and I\", se utiliza \"are\" y el pronombre correspondiente es \"we\".",
                listOf(
                    QSeed(
                        "¿Como quedaría la siguiente oración utilizando un pronombre personal?: Maria is a nurse",
                        listOf("He is a nurse", "She is a nurse", "They is a nurse", "We are a nurse"),
                        "She is a nurse"
                    ),
                    QSeed(
                        "¿Como quedaría la siguiente oración utilizando un pronombre personal?: Luis and I are Mexican",
                        listOf("We are Mexican", "They is Mexican", "He are Mexican", "I is Mexican"),
                        "We are Mexican"
                    )
                ),
                listOf(
                    VSeed("nurse", "enfermera", "/nɜːrs/"),
                    VSeed("Mexican", "mexicano", "/ˈmeksɪkən/")
                )
            ),
            ExSeed(
                "Verbo To Be negativo",
                1,
                "Uso del verbo to be en forma negativa",
                "We are not singers. He is not my brother.",
                "Para formar la forma negativa del verbo to be se agrega la palabra \"not\" después del verbo. También existen contracciones como \"aren't\". En la tercera persona del singular (he, she, it) se utiliza \"is\" y en negativo \"is not\" o su contracción \"isn't\".",
                listOf(
                    QSeed(
                        "Completa la siguiente frase en forma negativa: We ____ singers",
                        listOf("are not", "is not", "am not", "be not"),
                        "are not"
                    ),
                    QSeed(
                        "Completa la siguiente frase en forma negativa: He ____ my brother",
                        listOf("is not", "are not", "am not", "be not"),
                        "is not"
                    )
                ),
                listOf(
                    VSeed("singers", "cantantes", "/ˈsɪŋərz/"),
                    VSeed("brother", "hermano", "/ˈbrʌðər/")
                )
            ),
            ExSeed(
                "Verbo To Be interrogativo",
                1,
                "Formación de preguntas con el verbo to be",
                "They are my parents.",
                "La forma interrogativa del verbo to be se forma colocando el verbo antes del sujeto.",
                listOf(
                    QSeed(
                        "Ordena las siguientes palabras para formar la oración correcta: are / they / my / parents / ?",
                        listOf(
                            "Are they my parents ?",
                            "They are my parents ?",
                            "Are my parents they ?",
                            "They my parents are ?"
                        ),
                        "Are they my parents ?"
                    )
                ),
                listOf(
                    VSeed("parents", "padres", "/ˈperənts/")
                )
            ),
            ExSeed(
                "Rutinas diarias en contexto",
                3,
                "Uso del presente simple en historias más completas",
                "Maria is a doctor and she works in a hospital every day. She helps people and she is very kind with her patients. Juan is a teacher and he teaches English in a school. He loves his job and he works with many students every week.",
                "El presente simple se utiliza para describir rutinas diarias y hábitos. En tercera persona singular (she, he, it) el verbo generalmente termina en \"s\", como en \"works\" o \"helps\". En presente simple, la tercera persona del singular agrega \"s\" o \"es\" al verbo. Además, se usa para expresar acciones habituales como \"teaches\" o \"works\".",
                listOf(
                    QSeed(
                        "Selecciona la opción correcta: She ____ in a hospital every day",
                        listOf("work", "works", "working", "worked"),
                        "works"
                    ),
                    QSeed(
                        "Completa la oración: He ____ English in a school",
                        listOf("teach", "teaches", "teaching", "teached"),
                        "teaches"
                    )
                ),
                listOf(
                    VSeed("doctor", "doctor", "/ˈdɑːktər/"),
                    VSeed("hospital", "hospital", "/ˈhɑːspɪtəl/"),
                    VSeed("helps", "ayuda", "/helps/"),
                    VSeed("kind", "amable", "/kaɪnd/"),
                    VSeed("patients", "pacientes", "/ˈpeɪʃənts/"),
                    VSeed("teaches", "enseña", "/ˈtiːtʃɪz/"),
                    VSeed("school", "escuela", "/skuːl/"),
                    VSeed("loves", "ama", "/lʌvz/"),
                    VSeed("job", "trabajo", "/dʒɑːb/"),
                    VSeed("students", "estudiantes", "/ˈstuːdənts/")
                )
            ),
            ExSeed(
                "Vida diaria en familia",
                3,
                "Uso del presente simple con diferentes sujetos",
                "Ana and Luis are siblings and they live in a big house. They eat dinner together and they watch TV every night. I am a student and I study every day. I have classes in the morning and I do homework in the afternoon.",
                "El presente simple también se usa con sujetos en plural (they, we, you), donde el verbo no cambia y se usa en su forma base, como \"eat\" o \"watch\". El presente simple se usa con \"I\" en su forma base del verbo. Expresa rutinas como \"study\", \"have\" o \"do\". ",
                listOf(
                    QSeed(
                        "Selecciona la opción correcta: They ____ dinner together",
                        listOf("eat", "eats", "eating", "ate"),
                        "eat"
                    ),
                    QSeed(
                        "Completa la oración: I ____ every day",
                        listOf("study", "studies", "studying", "studied"),
                        "study"
                    )
                ),
                listOf(
                    VSeed("siblings", "hermanos", "/ˈsɪblɪŋz/"),
                    VSeed("live", "viven", "/lɪv/"),
                    VSeed("house", "casa", "/haʊs/"),
                    VSeed("dinner", "cena", "/ˈdɪnər/"),
                    VSeed("together", "juntos", "/təˈɡeðər/"),
                    VSeed("study", "estudiar", "/ˈstʌdi/"),
                    VSeed("classes", "clases", "/ˈklæsɪz/"),
                    VSeed("morning", "mañana", "/ˈmɔːrnɪŋ/"),
                    VSeed("homework", "tarea", "/ˈhoʊmwɜːrk/"),
                    VSeed("afternoon", "tarde", "/ˌæftərˈnuːn/")
                )
            ),
            ExSeed(
                "Rutinas y hábitos",
                3,
                "Uso del presente simple en diferentes contextos",
                "My parents work in an office and they travel to the city every week. They like their jobs and they are very responsible.",
                "El presente simple describe hábitos y rutinas. Con sujetos en plural como \"they\", el verbo se mantiene en su forma base como \"work\" o \"travel\". ",
                listOf(
                    QSeed(
                        "Selecciona la opción correcta: They ____ to the city every week",
                        listOf("travel", "travels", "traveling", "traveled"),
                        "travel"
                    )
                ),
                listOf(
                    VSeed("office", "oficina", "/ˈɔːfɪs/"),
                    VSeed("travel", "viajar", "/ˈtrævəl/"),
                    VSeed("city", "ciudad", "/ˈsɪti/"),
                    VSeed("week", "semana", "/wiːk/"),
                    VSeed("responsible", "responsable", "/rɪˈspɑːnsəbəl/")
                )
            )
        )

        var eOrder = 1
        seeds.forEach { s ->
            val exId = Exercises.insert {
                it[unitId] = s.unitId
                it[name] = s.name
                it[description] = s.desc
                it[orderExercise] = eOrder++
                it[isActive] = true
            }[Exercises.id]


            val ecId = ExerciseContent.insert {
                it[exerciseId] = exId.value
                it[contentType] = ContentType.READING
                it[textContent] = s.content
                it[grammarExplanation] = s.grammar
                it[audioUrl] = null
            }[ExerciseContent.id]

            s.vocab.forEach { v ->
                val wordId = Words.insert {
                    it[english] = v.english.trim()
                    it[spanish] = v.spanish.trim()
                    it[phonetic] = v.phonetic.trim()
                    it[description] = null
                    it[isActive] = true
                }[Words.id]

                ExerciseWords.insert {
                    it[exerciseId] = exId.value
                    it[this.wordId] = wordId.value
                }
            }

            var qOrder = 1
            s.qs.forEach { q ->
                val qId = Questions.insert {
                    it[exerciseContentId] = ecId.value
                    it[questionText] = q.text
                    it[orderQuestion] = qOrder++
                    it[isActive] = true
                }[Questions.id]

                q.alts.forEach { alt ->
                    Alternatives.insert {
                        it[questionId] = qId.value
                        it[text] = alt.trim()
                        it[isCorrect] = (alt.trim() == q.correct.trim())
                    }
                }
            }
        }
    }
}