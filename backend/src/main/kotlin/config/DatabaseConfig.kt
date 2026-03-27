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
import models.ExercisesOnHold
import models.Notifications
import models.ExerciseWords
import models.Questions
import models.Role
import models.TestCompleted
import models.TestExercises
import models.Tests
import models.UnitsCompleted
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
            ExercisesOnHold,
            ExerciseCompleted,
            Notifications,
            Words,
            Questions,
            Tests,
            TestExercises,
            UnitsCompleted,
            TestCompleted,
            ExerciseWords,
            Alternatives
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
        ExerciseContent.deleteAll()
        Exercises.deleteAll()

        data class QSeed(val text: String, val alts: List<String>, val correct: String)
        data class ExSeed(
            val name: String,
            val unitId: Int,
            val desc: String,
            val content: String,
            val grammar: String,
            val qs: List<QSeed>
        )

        val seeds = listOf(
            ExSeed(
                "Verbo To Be afirmativo", 1, "Verbo To Be en su forma afirmativa",
                "A continuación, se te presentan los pronombres personales en inglés conjugados con el verbo to be. [Pronombre][Verbo To be][Contracción][Traducción][Persona a la que corresponde] [I][am][I´m][Yo soy o estoy][Primera persona del singular]  [You][are][You´re][Tu eres o estás][Segunda persona del singular] [She,He,It][is][She´s,He´s,It´s][Ella es o está,Él es o está,Ello es o está][Tercera persona del singular][We][are][We´re][Nosotros somos o estamos][Primera persona del plural] [You][are][You´re][Ustedes son o están][Segunda persona del plural] [They][are][They´re][Ellos son o están][Tercera persona del plural]",
                "El verbo to be, que significa ser, estar o tener, según el uso que se le dé, es sin lugar a duda el verbo más utilizado en la lengua inglesa y también el más importante. Se utiliza como verbo principal y como auxiliar, y es irregular en pasado y en presente. En este momento aprenderás y / o reafirmarás el uso de este verbo to be en presente simple. Debes recordar que los pronombres personales son palabras que se utilizan en el discurso para hacer alusión a sustantivos sin nombrarlos, a partir de su persona gramatical.Ejemplo: Juan y Ana son esposos. = Ellos son esposos Roberto es un arquitecto muy famoso = Él es un arquitecto muy famoso De esta misma forma funcionan en inglés, si nosotros queremos sustituir un nombre, utilizamos un pronombre personal.",
                listOf(
                    QSeed(
                        "¿Como quedaría la siguiente oracion utilizando un pronombre personal?: Maria is a nurse",
                        listOf(
                            "He is a nurse",
                            "I is a nurse",
                            "Maria are a nurse",
                            "She is a nurse"
                        ),
                        "She is a nurse"
                    ),
                    QSeed(
                        "¿Como quedaría la siguiente oracion utilizando un pronombre personal?: Luis and I are Mexican",
                        listOf(
                            "I are Mexican",
                            "We are Mexican",
                            "She are Mexican",
                            " Luis and I is Mexican"
                        ),
                        "We are Mexican"
                    )
                )
            ),
            ExSeed(
                "Verbo To Be negativo", 1, "Verbo To Be en su forma negativa",
                "Al conjugar los verbos en inglés existen las formas negativas e interrogativas para expresar distintas ideas. Para formar la forma negativa del verbo to be debes agregar la palabra not después del verbo conjugado. [Pronombre personal][Forma negativa][Contracción o forma corta] [I][am not][I´m not] [You][are not][You aren’t] [She][is not][She isn’t] [He][is not][He isn’t] [It][is not][It isn’t] [We][are not][We aren’t] [You][are not][You aren’t] [They][are not][They aren’t]",
                "Al conjugar los verbos en inglés existen las formas negativas e interro gativas para expresar distintas ideas. Para formar la forma negativa del verbo to be debes agregar la palabra not después del verbo conjugado.",
                listOf(
                    QSeed(
                        "Completa la siguiente frase en forma negativa: We____singers",
                        listOf(
                            "We are singers",
                            " We is not singers",
                            "We are not singers",
                            "We not are singers "
                        ),
                        "We are not singers"
                    ),
                    QSeed(
                        "Completa la siguiente frase en forma negativa: He____my brother",
                        listOf(
                            "He is not my brother",
                            "He aren’t my brother",
                            "He is my brother",
                            "He not my brother"
                        ),
                        "He is not my brother"
                    )
                )
            ),
            ExSeed(
                "Verbo To Be interrogativo", 1, "Verbo To Be en su forma interrogativa",
                "Ejemplo de forma interrogativa: They are my parents = Ellos son mis padres  Forma interrogativa -> Are they my parents? = ¿Son ellos mis padres? ",
                "La forma interrogativa del verbo to be se forma anteponiendo el verbo antes que el sujeto y el pronombre personal.",
                listOf(
                    QSeed(
                        "Ordena las siguientes palabras para formar la oración correcta: sunny/ ? / it / is",
                        listOf(
                            "Is it sunny ?",
                            " sunny it is ?",
                            " it sunny is?",
                            " it is sunny ? "
                        ),
                        "Is it sunny ?"
                    )
                )
            ),
            ExSeed(
                "Adjetivos posesivos", 1, "Uso de adjetivos posesivos en inglés",
                "Pronombre personal y adjetivo posesivo correspondiente: [Pronombre][Adjetivo posesivo][Traducción] [I][my][mi/mis] [you][your][tu/tus] [he][his][su de él] [she][her][su de ella] [it][its][su de ello] [we][our][nuestro/nuestra] [they][their][su de ellos]",
                "En inglés los adjetivos posesivos indican a quién pertenece un sustantivo. Estos adjetivos se colocan antes del sustantivo para mostrar quién o qué lo posee. Los principales son my, your, his, her, its, our y their.",
                listOf(
                    QSeed(
                        "Completa la oración con el adjetivo posesivo correcto: I think ____ cat is beautiful",
                        listOf("her", "my", "his", "their"),
                        "her"
                    ),
                    QSeed(
                        "Completa la oración con el adjetivo posesivo correcto: These are ____ books",
                        listOf("your", "his", "my", "their"),
                        "my"
                    )
                )
            ),
            ExSeed(
                "Adjetivos posesivos en oraciones",
                1,
                "Uso de adjetivos posesivos en objetos personales",
                "Ejemplos del uso de adjetivos posesivos: I think her cat is beautiful That is my pencil These are my books I cut my hair every month",
                "En inglés es común utilizar adjetivos posesivos con objetos personales o partes del cuerpo para indicar a quién pertenecen. Estos adjetivos siempre se colocan antes del sustantivo.",
                listOf(
                    QSeed(
                        "Completa la oración con el adjetivo posesivo correcto: I cut ____ hair every month",
                        listOf("her", "our", "their", "my"),
                        "my"
                    ),
                    QSeed(
                        "Completa la oración con el adjetivo posesivo correcto: That is ____ pencil",
                        listOf("my", "your", "his", "their"),
                        "my"
                    )
                )
            ),
            ExSeed(
                "Posesivo anglosajón", 1, "Uso del genitivo sajón para expresar posesión",
                "Ejemplos de posesivo anglosajón: My sister's cat Ana's mother Laura's dog",
                "El posesivo anglosajón o genitivo sajón se usa en inglés para indicar que algo pertenece a alguien. Se forma agregando un apóstrofe y la letra s ('s) al sustantivo que posee algo, por ejemplo: My sister's cat.",
                listOf(
                    QSeed(
                        "Selecciona la oración correcta para expresar: El perro de Laura",
                        listOf("Laura's dog", "Laura dog", "Laura dog's", "Laura dogs"),
                        "Laura's dog"
                    ),
                    QSeed(
                        "Selecciona la oración correcta para expresar: La mamá de Ana",
                        listOf("Ana mother", "Ana's mother", "Ana mothers'", "Ana's mothers"),
                        "Ana's mother"
                    )
                )
            ),
            ExSeed(
                "Presente simple afirmativo", 1, "Uso del presente simple para describir rutinas",
                "Estructura del presente simple afirmativo: [Subject][Verb][Complement] [I][play][with a ball] [He][plays][with a ball] [We][play][with a ball]",
                "Utilizamos el presente simple para expresar acciones que ocurren regularmente como rutinas, hábitos o actividades cotidianas. Para formar una oración afirmativa se utiliza la estructura sujeto + verbo + complemento. En tercera persona singular (he, she, it) el verbo generalmente termina en “s”.",
                listOf(
                    QSeed(
                        "Corrige la siguiente oración: He watchs TV everyday.",
                        listOf(
                            "He watches TV everyday.",
                            "He watch TV everyday.",
                            "He watching TV everyday.",
                            "He watches TV everydays."
                        ),
                        "He watches TV everyday."
                    ),
                    QSeed(
                        "Corrige la siguiente oración: She work in an office.",
                        listOf(
                            "She works in an office.",
                            "She working in an office.",
                            "She works at office.",
                            "She workes in an office."
                        ),
                        "She works in an office."
                    )
                )
            ),
            ExSeed(
                "Presente simple tercera persona",
                1,
                "Conjugación del verbo en tercera persona singular",
                "Ejemplos de presente simple: I play with a ball He plays with a ball She plays with a ball",
                "En el presente simple los verbos cambian en tercera persona del singular (he, she, it). Generalmente se agrega la letra “s” al verbo para indicar que la acción la realiza una tercera persona.",
                listOf(
                    QSeed(
                        "Selecciona la oración correcta en presente simple: Bobby crys all nights",
                        listOf(
                            "Bobby cries all nights.",
                            "Bobby cry all nights.",
                            "Bobby crys all nights.",
                            "Bobby crying all nights."
                        ),
                        "Bobby cries all nights."
                    )
                )
            ),
            ExSeed(
                "Presente simple negativo", 1, "Uso de auxiliares do y does en forma negativa",
                "Forma negativa del presente simple: [Subject][Auxiliar][Not][Verb] [I][do][not][play] [He][does][not][play] [They][do][not][play]",
                "Para formar la forma negativa en presente simple se utilizan los auxiliares do o does después del sujeto, seguidos de la negación not. Las terceras personas del singular (he, she, it) utilizan does y el verbo principal no lleva la terminación “s”.",
                listOf(
                    QSeed(
                        "Completa la oración en forma negativa: She ____ work on Sundays.",
                        listOf("do not", "doesn't works", "do doesn't", "does not"),
                        "does not"
                    ),
                    QSeed(
                        "Completa la oración en forma negativa: They ____ cut the grass in the garden.",
                        listOf("do nots", "do not", "does not", "doesn't"),
                        "do not"
                    )
                )
            ),
            ExSeed(
                "Presente simple negativo contracciones",
                1,
                "Uso de contracciones en presente simple",
                "Forma negativa larga y corta: [I][do not][don't] [He][does not][doesn't] [They][do not][don't]",
                "La forma negativa del presente simple puede expresarse de forma larga (do not / does not) o con contracciones (don't / doesn't). Ambas formas tienen el mismo significado.",
                listOf(
                    QSeed(
                        "Selecciona la contracción correcta de: He does not play soccer.",
                        listOf(
                            "He don't play soccer.",
                            "He doesn't play soccer.",
                            "He dont play soccer.",
                            "He does'nt play soccer."
                        ),
                        "He doesn't play soccer."
                    )
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