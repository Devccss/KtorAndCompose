package config

import com.example.dtos.CreateUnitDto
import com.example.dtos.UnitDto
import com.example.dtos.UserDto
import io.github.cdimascio.dotenv.dotenv
import models.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

// Data classes for seeds
data class QuestionSeed(val text: String, val alternatives: List<String>, val correct: String)
data class VocabularySeed(val english: String, val spanish: String, val phonetic: String)
data class ExerciseSeed(
    val id: Int,
    val name: String,
    val unitId: Int,
    val description: String,
    val content: String,
    val grammar: String,
    val questions: List<QuestionSeed>,
    val vocabulary: List<VocabularySeed>
)

data class TestSeed(
    val name: String,
    val unitId: Int,
    val description: String,
    val exerciseIds: List<Int>
)

data class CompletedExerciseSeed(val userId: Int, val exerciseId: Int)
data class CompletedUnitSeed(val userId: Int, val unitId: Int)
data class TestCompletedSeed(val userId: Int, val testId: Int, val score: Int)
data class SessionLogSeed(
    val userId: Int,
    val loginAt: LocalDateTime,
    val logoutAt: LocalDateTime?,
    val endReason: SessionEndReason? = null
)

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
    val studentExists = Users.selectAll().where { Users.email eq studentEmail }.count() > 0
    if (!studentExists) {
        studentsCreate(studentPassword, studentEmail, student)
    }
}


fun studentsCreate(studentPassword: String, studentEmail: String, student: String) {
    val hashed = BCrypt.hashpw(studentPassword, BCrypt.gensalt())
    Users.insert {
        it[email] = studentEmail
        it[password] = hashed
        it[name] = student
        it[preferences] = null
        it[provider] = "Created"
        it[currentUnitId] = null
        it[role] = Role.STUDENT
    }

    val newid2 = Users.insert {
        it[email] = "student2@2026"
        it[password] = hashed
        it[name] = "student2"
        it[preferences] = null
        it[provider] = "Created"
        it[currentUnitId] = null
        it[role] = Role.STUDENT
    }

    Users.insert {
        it[email] = "student3@2026"
        it[password] = hashed
        it[name] = "student3"
        it[preferences] = null
        it[provider] = "Created"
        it[currentUnitId] = null
    }

    Users.insert {
        it[email] = "student4@2026"
        it[password] = hashed
        it[name] = "student4"
        it[preferences] = null
        it[provider] = "Created"
        it[currentUnitId] = null
    }

    Users.insert {
        it[email] = "student5@2026"
        it[password] = hashed
        it[name] = "student5"
        it[preferences] = null
        it[provider] = "Created"
        it[currentUnitId] = null
    }

}

object SeedDataProvider {

    private var exerciseCounter = 0

    fun getUnitsSeeds(): List<UnitDto> {
        return listOf(
            UnitDto(
                id = 1,
                difficulty = DifficultyLevel.A1,
                name = "Unidad 1: Verbo to be",
                description = "Introducción al verbo 'to be'",
                orderUnit = 2,
                isActive = true,
                createdAt = "2026-01-01T00:00:00"
            ),

            UnitDto(
                id = 2,
                difficulty = DifficultyLevel.A1,
                name = "Unidad 2: Present Simple",
                description = "Rutinas, hábitos y acciones frecuentes",
                orderUnit = 3,
                isActive = true,
                createdAt = "2026-01-01T00:00:00"
            ),

            UnitDto(
                id = 3,
                difficulty = DifficultyLevel.A1,
                name = "Unidad 3: Family and Daily Life",
                description = "Vocabulario familiar y actividades cotidianas",
                orderUnit = 4,
                isActive = true,
                createdAt = "2026-01-01T00:00:00"
            ),

            UnitDto(
                id = 4,
                difficulty = DifficultyLevel.A1,
                name = "Unidad 4: Present Continuous",
                description = "Acciones que ocurren en este momento",
                orderUnit = 5,
                isActive = true,
                createdAt = "2026-01-01T00:00:00"
            ),

            UnitDto(
                id = 5,
                difficulty = DifficultyLevel.A2,
                name = "Unidad 5: Past Simple",
                description = "Eventos y experiencias del pasado",
                orderUnit = 6,
                isActive = true,
                createdAt = "2026-01-01T00:00:00"
            ),

            UnitDto(
                id = 6,
                difficulty = DifficultyLevel.A2,
                name = "Unidad 6: Future Forms",
                description = "Uso de will y going to",
                orderUnit = 7,
                isActive = true,
                createdAt = "2026-01-01T00:00:00"
            )
        )

    }

    fun getExerciseSeeds(): List<ExerciseSeed> {
        return listOf(
            // ===== UNIDAD 1: A1 - To Be =====
            ExerciseSeed(
                id = exerciseCounter++,
                name = "To Be - Pronombres Básicos",
                unitId = 1,
                description = "Introducción al verbo 'to be' con pronombres personales",
                content = "I am a student. You are my friend. He is a teacher. She is a doctor. It is a cat. We are colleagues. They are friends.",
                grammar = "El verbo 'to be' es el verbo más importante en inglés. Se conjuga como: I am, you are, he/she/it is, we are, they are.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: ___ am a student",
                        listOf("I", "You", "He", "She"),
                        "I"
                    ),
                    QuestionSeed(
                        "Which is correct?",
                        listOf(
                            "She are a teacher",
                            "She is a teacher",
                            "She am a teacher",
                            "She be a teacher"
                        ),
                        "She is a teacher"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("student", "estudiante", "/ˈstuːdənt/"),
                    VocabularySeed("friend", "amigo", "/frend/"),
                    VocabularySeed("teacher", "profesor", "/ˈtiːtʃər/"),
                    VocabularySeed("doctor", "doctor", "/ˈdɑːktər/"),
                    VocabularySeed("cat", "gato", "/kæt/"),
                    VocabularySeed("colleagues", "colegas", "/ˈkɑːliːɡz/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "To Be Negativo",
                unitId = 1,
                description = "Formar oraciones negativas con 'to be'",
                content = "I am not a teacher. You are not a doctor. He is not a student. We are not lazy. They are not here.",
                grammar = "Para negativos, agregamos 'not' después del verbo: I am not, you are not, he/she/it is not, we are not, they are not. Contracciones: isn't, aren't, 'm not",
                questions = listOf(
                    QuestionSeed(
                        "Complete: I ___ not happy",
                        listOf("am", "are", "is", "be"),
                        "am"
                    ),
                    QuestionSeed(
                        "Which is the contraction of 'is not'?",
                        listOf("aren't", "isn't", "am not", "'m not"),
                        "isn't"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("lazy", "perezoso", "/ˈleɪzi/"),
                    VocabularySeed("here", "aquí", "/hɪr/"),
                    VocabularySeed("happy", "feliz", "/ˈhæpi/"),
                    VocabularySeed("tired", "cansado", "/ˈtaɪərd/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "To Be Interrogativo",
                unitId = 1,
                description = "Hacer preguntas con 'to be'",
                content = "Am I right? Are you ready? Is he a student? Is she a doctor? Are we late? Are they coming?",
                grammar = "Para preguntas, colocamos el verbo antes del sujeto: Am I...? Are you...? Is he/she/it...? Are we...? Are they...?",
                questions = listOf(
                    QuestionSeed(
                        "Order: are / you / ready / ?",
                        listOf(
                            "Are you ready?",
                            "You are ready?",
                            "Ready are you?",
                            "Are ready you?"
                        ),
                        "Are you ready?"
                    ),
                    QuestionSeed(
                        "Complete: ___ you a teacher?",
                        listOf("Are", "Is", "Am", "Be"),
                        "Are"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("right", "correcto", "/raɪt/"),
                    VocabularySeed("ready", "listo", "/ˈredi/"),
                    VocabularySeed("late", "tarde", "/leɪt/"),
                    VocabularySeed("coming", "viniendo", "/ˈkʌmɪŋ/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Descripciones Personales",
                unitId = 1,
                description = "Usar 'to be' para describir características personales",
                content = "I am 25 years old. She is beautiful. He is intelligent. We are Spanish. They are Brazilian.",
                grammar = "Usamos 'to be' para describir edad, apariencia, nacionalidad, profesión y características.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: They ___ from Mexico",
                        listOf("am", "is", "are", "be"),
                        "are"
                    ),
                    QuestionSeed(
                        "Which sentence is correct?",
                        listOf(
                            "She is intelligents",
                            "She are intelligent",
                            "She is intelligent",
                            "She intelligent"
                        ),
                        "She is intelligent"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("years old", "años de edad", "/jɪrz oʊld/"),
                    VocabularySeed("beautiful", "hermosa", "/ˈbjuːtɪfl/"),
                    VocabularySeed("intelligent", "inteligente", "/ɪnˈtɛlɪdʒənt/"),
                    VocabularySeed("Spanish", "español", "/ˈspænɪʃ/"),
                    VocabularySeed("Brazilian", "brasileño", "/brəˈzɪliən/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Profesiones y Ocupaciones",
                unitId = 1,
                description = "Expresar profesiones y ocupaciones con 'to be'",
                content = "I am an engineer. You are a nurse. He is a chef. She is an architect. We are developers.",
                grammar = "Usamos 'a' o 'an' (a + consonante, an + vocal) antes de profesiones singulares.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: He ___ ___ doctor",
                        listOf("is a", "is an", "are a", "are an"),
                        "is a"
                    ),
                    QuestionSeed(
                        "Which is correct?",
                        listOf(
                            "She am an actress",
                            "She is an actress",
                            "She are actress",
                            "She be an actress"
                        ),
                        "She is an actress"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("engineer", "ingeniero", "/ˌɛndʒɪˈnɪr/"),
                    VocabularySeed("nurse", "enfermera", "/nɜːrs/"),
                    VocabularySeed("chef", "cocinero", "/ʃef/"),
                    VocabularySeed("architect", "arquitecto", "/ˈɑːrkɪtekt/"),
                    VocabularySeed("developer", "desarrollador", "/dɪˈvɛləpər/"),
                    VocabularySeed("actress", "actriz", "/ˈæktres/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Estados y Sentimientos",
                unitId = 1,
                description = "Expresar estados emocionales y físicos",
                content = "I am happy. You are sad. He is tired. She is excited. We are confused. They are worried.",
                grammar = "Para expresar sentimientos y estados físicos, usamos 'to be' + adjetivo.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: They ___ very excited about the trip",
                        listOf("am", "is", "are", "be"),
                        "are"
                    ),
                    QuestionSeed(
                        "Which expresses a feeling?",
                        listOf("I am tall", "I am happy", "I am 25", "I am from Spain"),
                        "I am happy"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("happy", "feliz", "/ˈhæpi/"),
                    VocabularySeed("sad", "triste", "/sæd/"),
                    VocabularySeed("tired", "cansado", "/ˈtaɪərd/"),
                    VocabularySeed("excited", "emocionado", "/ɪkˈsaɪtɪd/"),
                    VocabularySeed("confused", "confundido", "/kənˈfjuːzd/"),
                    VocabularySeed("worried", "preocupado", "/ˈwɜːrid/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Ubicaciones y Lugares",
                unitId = 1,
                description = "Usar 'to be' para hablar de ubicaciones",
                content = "I am at home. You are in the office. He is at the beach. She is in the library. We are at school.",
                grammar = "Para indicar ubicación: to be + preposición (at, in, on) + lugar",
                questions = listOf(
                    QuestionSeed(
                        "Complete: They ___ ___ the park",
                        listOf("are in", "is on", "am at", "are at"),
                        "are in"
                    ),
                    QuestionSeed(
                        "Where is he?",
                        listOf(
                            "He is at the beach",
                            "He am at beach",
                            "He is beach",
                            "He on the beach"
                        ),
                        "He is at the beach"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("home", "casa", "/hoʊm/"),
                    VocabularySeed("office", "oficina", "/ˈɑːfɪs/"),
                    VocabularySeed("beach", "playa", "/biːtʃ/"),
                    VocabularySeed("library", "biblioteca", "/ˈlaɪbreri/"),
                    VocabularySeed("school", "escuela", "/skuːl/"),
                    VocabularySeed("park", "parque", "/pɑːrk/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Objetos y Cosas",
                unitId = 1,
                description = "Usar 'to be' para describir objetos y cosas",
                content = "The book is interesting. The laptop is expensive. The car is fast. The weather is sunny. The food is delicious.",
                grammar = "El sujeto puede ser también una cosa u objeto. La conjugación sigue siendo igual.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The movie ___ amazing",
                        listOf("am", "is", "are", "be"),
                        "is"
                    ),
                    QuestionSeed(
                        "Which is correct?",
                        listOf(
                            "The books is new",
                            "The books are new",
                            "The books am new",
                            "The book are new"
                        ),
                        "The books are new"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("book", "libro", "/bʊk/"),
                    VocabularySeed("interesting", "interesante", "/ˈɪntrəstɪŋ/"),
                    VocabularySeed("laptop", "computadora portátil", "/ˈlæptɑːp/"),
                    VocabularySeed("expensive", "caro", "/ɪkˈspɛnsɪv/"),
                    VocabularySeed("fast", "rápido", "/fæst/"),
                    VocabularySeed("delicious", "delicioso", "/dɪˈlɪʃəs/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Tiempo y Clima",
                unitId = 1,
                description = "Describir el tiempo y clima",
                content = "It is sunny today. It is cold in winter. It is hot in summer. It is rainy in spring.",
                grammar = "Para hablar del clima, usamos 'It is' + adjetivo de clima o 'It is' + nombre del clima.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: It ___ very cloudy today",
                        listOf("am", "is", "are", "be"),
                        "is"
                    ),
                    QuestionSeed(
                        "What do we use for weather?",
                        listOf("I am sunny", "You are rainy", "It is cold", "They is windy"),
                        "It is cold"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("sunny", "soleado", "/ˈsʌni/"),
                    VocabularySeed("cold", "frío", "/koʊld/"),
                    VocabularySeed("hot", "caliente", "/hɑːt/"),
                    VocabularySeed("rainy", "lluvioso", "/ˈreɪni/"),
                    VocabularySeed("cloudy", "nublado", "/ˈklaʊdi/"),
                    VocabularySeed("windy", "ventoso", "/ˈwɪndi/")
                )
            ),
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Repaso General - To Be",
                unitId = 1,
                description = "Repaso completo de 'to be' en contexto",
                content = "My name is John. I am 30 years old. I am an engineer. I am from Spain. My job is interesting. My family is large. We are very close. They are important to me.",
                grammar = "Recapitulación: 'to be' para nombre, edad, profesión, origen, descripciones y relaciones.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: My sister ___ a lawyer",
                        listOf("am", "is", "are", "be"),
                        "is"
                    ),
                    QuestionSeed(
                        "Which sentences are correct?",
                        listOf(
                            "I is from England / You are from France",
                            "I am from England / You are from France",
                            "I are from England / You is from France",
                            "I be from England / You be from France"
                        ),
                        "I am from England / You are from France"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("name", "nombre", "/neɪm/"),
                    VocabularySeed("large", "grande", "/lɑːrdʒ/"),
                    VocabularySeed("close", "cercano", "/kloʊs/"),
                    VocabularySeed("important", "importante", "/ɪmˈpɔːrtənt/"),
                    VocabularySeed("lawyer", "abogado", "/ˈlɔːjər/"),
                    VocabularySeed("family", "familia", "/ˈfæməli/")
                )
            ),

            //UNIDAD 2 - PRESENT SIMPLE
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Simple - Doctor's Routine",
                unitId = 2,
                description = "Uso del presente simple para describir rutinas diarias",
                content = "Maria is a doctor. She works in a hospital every day. She helps patients and checks their health.",
                grammar = "El presente simple se utiliza para describir hábitos y rutinas. En tercera persona singular (he, she, it) el verbo generalmente termina en 's' o 'es'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: She ____ in a hospital every day.",
                        listOf("work", "works", "working", "worked"),
                        "works"
                    ),
                    QuestionSeed(
                        "Choose the correct option:",
                        listOf(
                            "She help patients",
                            "She helps patients",
                            "She helping patients",
                            "She helped patients"
                        ),
                        "She helps patients"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("doctor", "doctor", "/ˈdɑːktər/"),
                    VocabularySeed("hospital", "hospital", "/ˈhɑːspɪtəl/"),
                    VocabularySeed("patients", "pacientes", "/ˈpeɪʃənts/"),
                    VocabularySeed("health", "salud", "/helθ/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Simple - Teacher's Job",
                unitId = 2,
                description = "Uso del presente simple con profesiones",
                content = "John is a teacher. He teaches English at school and prepares lessons every week.",
                grammar = "Cuando el verbo termina en ciertos sonidos, la tercera persona singular agrega 'es', como en teaches.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: He ____ English at school.",
                        listOf("teach", "teaches", "teaching", "taught"),
                        "teaches"
                    ),
                    QuestionSeed(
                        "Complete: He ____ lessons every week.",
                        listOf("prepare", "prepares", "preparing", "prepared"),
                        "prepares"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("teacher", "profesor", "/ˈtiːtʃər/"),
                    VocabularySeed("school", "escuela", "/skuːl/"),
                    VocabularySeed("lessons", "lecciones", "/ˈlesənz/"),
                    VocabularySeed("English", "inglés", "/ˈɪŋɡlɪʃ/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Simple - Family Life",
                unitId = 2,
                description = "Uso del presente simple con sujetos plurales",
                content = "My parents live in a small town. They work together and spend time with the family every weekend.",
                grammar = "Con sujetos plurales como 'they', el verbo permanece en su forma base sin agregar 's'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: They ____ together.",
                        listOf("works", "work", "working", "worked"),
                        "work"
                    ),
                    QuestionSeed(
                        "Choose the correct option:",
                        listOf(
                            "They spends time together",
                            "They spend time together",
                            "They spending time together",
                            "They spent time together"
                        ),
                        "They spend time together"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("parents", "padres", "/ˈperənts/"),
                    VocabularySeed("town", "ciudad pequeña", "/taʊn/"),
                    VocabularySeed("family", "familia", "/ˈfæməli/"),
                    VocabularySeed("weekend", "fin de semana", "/ˌwiːkˈend/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Simple - Student Routine",
                unitId = 2,
                description = "Rutinas académicas en presente simple",
                content = "I study computer science. I attend classes every morning and do homework every afternoon.",
                grammar = "Con el pronombre 'I', el verbo se utiliza en su forma base. El presente simple expresa acciones habituales.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: I ____ computer science.",
                        listOf("studies", "study", "studying", "studied"),
                        "study"
                    ),
                    QuestionSeed(
                        "Complete: I ____ homework every afternoon.",
                        listOf("do", "does", "doing", "did"),
                        "do"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("computer science", "informática", "/kəmˈpjuːtər ˈsaɪəns/"),
                    VocabularySeed("classes", "clases", "/ˈklæsɪz/"),
                    VocabularySeed("homework", "tarea", "/ˈhoʊmwɜːrk/"),
                    VocabularySeed("afternoon", "tarde", "/ˌæftərˈnuːn/")
                )
            ),

            //UNIDAD 3 - FAMILY AND DAILY LIFE
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Studying in the Library",
                unitId = 3,
                description = "Uso del presente continuo para acciones que ocurren en este momento",
                content = "Maria is studying in the university library. She is reading a book about computer science and taking notes for her next exam. Her classmates are also working quietly around her.",
                grammar = "El presente continuo se utiliza para describir acciones que están ocurriendo en este momento. Se forma con el verbo 'to be' seguido del verbo principal terminado en '-ing'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Maria ____ in the library.",
                        listOf("studies", "is studying", "study", "studied"),
                        "is studying"
                    ),
                    QuestionSeed(
                        "Complete: She ____ notes for her exam.",
                        listOf("takes", "is taking", "take", "took"),
                        "is taking"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("library", "biblioteca", "/ˈlaɪbreri/"),
                    VocabularySeed("reading", "leyendo", "/ˈriːdɪŋ/"),
                    VocabularySeed("notes", "apuntes", "/noʊts/"),
                    VocabularySeed("exam", "examen", "/ɪɡˈzæm/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Working from Home",
                unitId = 3,
                description = "Acciones temporales en progreso",
                content = "John is working from home today because his office is closed. He is answering emails, attending online meetings and preparing a report for his manager.",
                grammar = "El presente continuo también puede utilizarse para situaciones temporales que ocurren alrededor del momento actual.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: John ____ from home today.",
                        listOf("works", "is working", "work", "worked"),
                        "is working"
                    ),
                    QuestionSeed(
                        "Choose the correct option:",
                        listOf(
                            "He is answering emails",
                            "He answering emails",
                            "He answers emails now",
                            "He answered emails"
                        ),
                        "He is answering emails"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("office", "oficina", "/ˈɔːfɪs/"),
                    VocabularySeed("emails", "correos electrónicos", "/ˈiːmeɪlz/"),
                    VocabularySeed("meeting", "reunión", "/ˈmiːtɪŋ/"),
                    VocabularySeed("report", "informe", "/rɪˈpɔːrt/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Classroom Activity",
                unitId = 3,
                description = "Descripción de actividades que ocurren actualmente",
                content = "The students are listening to the teacher during the English lesson. They are completing exercises and discussing the answers with their classmates.",
                grammar = "Con sujetos plurales como 'students' o 'they', se utiliza 'are' seguido del verbo terminado en '-ing'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The students ____ to the teacher.",
                        listOf("listen", "are listening", "listened", "listening"),
                        "are listening"
                    ),
                    QuestionSeed(
                        "Complete: They ____ exercises.",
                        listOf("complete", "are completing", "completed", "completes"),
                        "are completing"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("lesson", "lección", "/ˈlesən/"),
                    VocabularySeed("teacher", "profesor", "/ˈtiːtʃər/"),
                    VocabularySeed("exercises", "ejercicios", "/ˈeksərsaɪzɪz/"),
                    VocabularySeed("classmates", "compañeros de clase", "/ˈklæsmeɪts/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Football Practice",
                unitId = 3,
                description = "Uso del presente continuo en actividades deportivas",
                content = "The football team is preparing for an important tournament. The players are running around the field, practicing passes and improving their physical condition.",
                grammar = "El presente continuo permite describir acciones en desarrollo y procesos que están ocurriendo actualmente.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The football team ____ for a tournament.",
                        listOf("prepares", "is preparing", "prepare", "prepared"),
                        "is preparing"
                    ),
                    QuestionSeed(
                        "Complete: The players ____ around the field.",
                        listOf("run", "are running", "ran", "runs"),
                        "are running"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("tournament", "torneo", "/ˈtʊrnəmənt/"),
                    VocabularySeed("players", "jugadores", "/ˈpleɪərz/"),
                    VocabularySeed("field", "cancha", "/fiːld/"),
                    VocabularySeed("practice", "practicar", "/ˈpræktɪs/")
                )
            ),

            //UNIDAD 4 - PRESENT CONTINUOUS
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - At the Cafeteria",
                unitId = 4,
                description = "Acciones que están ocurriendo en este momento",
                content = "Several students are sitting in the university cafeteria. Some are eating lunch, while others are talking about their classes. A group of friends is studying for an upcoming exam.",
                grammar = "El presente continuo describe acciones que están ocurriendo en el momento de hablar. Se forma con el verbo 'to be' más un verbo terminado en '-ing'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Some students ____ lunch.",
                        listOf("eat", "eats", "are eating", "ate"),
                        "are eating"
                    ),
                    QuestionSeed(
                        "Complete: A group of friends ____ for an exam.",
                        listOf("study", "studies", "is studying", "studied"),
                        "is studying"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("cafeteria", "cafetería", "/ˌkæfəˈtɪriə/"),
                    VocabularySeed("lunch", "almuerzo", "/lʌntʃ/"),
                    VocabularySeed("classes", "clases", "/ˈklæsɪz/"),
                    VocabularySeed("upcoming", "próximo", "/ˈʌpkʌmɪŋ/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Office Work",
                unitId = 4,
                description = "Actividades laborales en desarrollo",
                content = "Emily is working at her office today. She is writing reports, answering emails and participating in an online meeting with clients from different countries.",
                grammar = "El presente continuo también se utiliza para describir actividades temporales que están ocurriendo actualmente.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Emily ____ reports.",
                        listOf("writes", "write", "is writing", "wrote"),
                        "is writing"
                    ),
                    QuestionSeed(
                        "Choose the correct option:",
                        listOf(
                            "She is answering emails",
                            "She answering emails",
                            "She answers emails now",
                            "She answered emails"
                        ),
                        "She is answering emails"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("reports", "informes", "/rɪˈpɔːrts/"),
                    VocabularySeed("emails", "correos electrónicos", "/ˈiːmeɪlz/"),
                    VocabularySeed("meeting", "reunión", "/ˈmiːtɪŋ/"),
                    VocabularySeed("clients", "clientes", "/ˈklaɪənts/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Sports Training",
                unitId = 4,
                description = "Acciones físicas que ocurren actualmente",
                content = "The basketball team is preparing for an important championship. The players are practicing new strategies and improving their teamwork during training sessions.",
                grammar = "Con sujetos plurales se utiliza 'are' + verbo terminado en '-ing'. Con sujetos singulares se utiliza 'is'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The players ____ new strategies.",
                        listOf("practice", "practices", "are practicing", "practiced"),
                        "are practicing"
                    ),
                    QuestionSeed(
                        "Complete: The team ____ for a championship.",
                        listOf("prepare", "prepares", "is preparing", "prepared"),
                        "is preparing"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("championship", "campeonato", "/ˈtʃæmpiənʃɪp/"),
                    VocabularySeed("strategies", "estrategias", "/ˈstrætədʒiz/"),
                    VocabularySeed("teamwork", "trabajo en equipo", "/ˈtiːmwɜːrk/"),
                    VocabularySeed("training", "entrenamiento", "/ˈtreɪnɪŋ/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Technology Project",
                unitId = 4,
                description = "Describir proyectos en progreso",
                content = "A group of engineering students is developing a mobile application. They are designing interfaces, testing features and solving technical problems as part of their final project.",
                grammar = "El presente continuo es útil para describir proyectos o procesos que se encuentran en desarrollo durante un período temporal.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The students ____ a mobile application.",
                        listOf("develop", "develops", "are developing", "developed"),
                        "are developing"
                    ),
                    QuestionSeed(
                        "Complete: They ____ technical problems.",
                        listOf("solve", "solves", "are solving", "solved"),
                        "are solving"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("engineering", "ingeniería", "/ˌendʒɪˈnɪrɪŋ/"),
                    VocabularySeed("application", "aplicación", "/ˌæplɪˈkeɪʃən/"),
                    VocabularySeed("interfaces", "interfaces", "/ˈɪntərfeɪsɪz/"),
                    VocabularySeed("features", "funcionalidades", "/ˈfiːtʃərz/")
                )
            ),

            //Unidad 5 - FUTURE SIMPLE
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Past Simple - Weekend Activities",
                unitId = 5,
                description = "Uso del pasado simple para acciones completadas",
                content = "Last weekend, Maria visited her grandparents in another city. She traveled by bus, spent time with her family and returned home on Sunday evening.",
                grammar = "El pasado simple se utiliza para describir acciones terminadas en un momento específico del pasado. Los verbos regulares generalmente terminan en '-ed'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Maria ____ her grandparents last weekend.",
                        listOf("visit", "visits", "visited", "visiting"),
                        "visited"
                    ),
                    QuestionSeed(
                        "Complete: She ____ by bus.",
                        listOf("travel", "travels", "traveled", "traveling"),
                        "traveled"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("weekend", "fin de semana", "/ˌwiːkˈend/"),
                    VocabularySeed("grandparents", "abuelos", "/ˈɡrænˌperənts/"),
                    VocabularySeed("traveled", "viajó", "/ˈtrævəld/"),
                    VocabularySeed("returned", "regresó", "/rɪˈtɜːrnd/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Past Simple - School Project",
                unitId = 5,
                description = "Pasado simple con verbos regulares",
                content = "John worked on a science project last month. He researched information on the internet, created a presentation and presented it to his classmates.",
                grammar = "Los verbos regulares forman el pasado agregando '-ed'. Algunos ejemplos son worked, researched y created.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: John ____ on a science project.",
                        listOf("work", "works", "worked", "working"),
                        "worked"
                    ),
                    QuestionSeed(
                        "Choose the correct option:",
                        listOf(
                            "He created a presentation",
                            "He create a presentation",
                            "He creating a presentation",
                            "He creates a presentation"
                        ),
                        "He created a presentation"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("project", "proyecto", "/ˈprɑːdʒekt/"),
                    VocabularySeed("researched", "investigó", "/rɪˈsɜːrtʃt/"),
                    VocabularySeed("presentation", "presentación", "/ˌpriːzenˈteɪʃən/"),
                    VocabularySeed("internet", "internet", "/ˈɪntərnet/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Past Simple - Football Match",
                unitId = 5,
                description = "Uso del pasado simple en eventos deportivos",
                content = "The football team played an important match yesterday. The players trained hard during the week and won the game by two goals.",
                grammar = "El pasado simple describe acciones que ocurrieron y finalizaron en el pasado. Expresiones como yesterday o last week suelen acompañarlo.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The team ____ an important match yesterday.",
                        listOf("play", "plays", "played", "playing"),
                        "played"
                    ),
                    QuestionSeed(
                        "Complete: The players ____ hard during the week.",
                        listOf("train", "trained", "training", "trains"),
                        "trained"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("match", "partido", "/mætʃ/"),
                    VocabularySeed("trained", "entrenaron", "/treɪnd/"),
                    VocabularySeed("won", "ganaron", "/wʌn/"),
                    VocabularySeed("goals", "goles", "/ɡoʊlz/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Past Simple - Family Vacation",
                unitId = 5,
                description = "Narración de experiencias pasadas",
                content = "Last summer, my family visited the coast. We stayed in a small hotel, walked along the beach and enjoyed the local food every day.",
                grammar = "El pasado simple permite narrar experiencias y eventos ocurridos en el pasado utilizando verbos regulares e irregulares.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: My family ____ the coast last summer.",
                        listOf("visit", "visits", "visited", "visiting"),
                        "visited"
                    ),
                    QuestionSeed(
                        "Complete: We ____ in a small hotel.",
                        listOf("stay", "stayed", "staying", "stays"),
                        "stayed"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("summer", "verano", "/ˈsʌmər/"),
                    VocabularySeed("coast", "costa", "/koʊst/"),
                    VocabularySeed("beach", "playa", "/biːtʃ/"),
                    VocabularySeed("hotel", "hotel", "/hoʊˈtel/")
                )
            ),

            //UNIDAD 6 - Future Forms
            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - At the Cafeteria",
                unitId = 4,
                description = "Acciones que están ocurriendo en este momento",
                content = "Several students are sitting in the university cafeteria. Some are eating lunch, while others are talking about their classes. A group of friends is studying for an upcoming exam.",
                grammar = "El presente continuo describe acciones que están ocurriendo en el momento de hablar. Se forma con el verbo 'to be' más un verbo terminado en '-ing'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Some students ____ lunch.",
                        listOf("eat", "eats", "are eating", "ate"),
                        "are eating"
                    ),
                    QuestionSeed(
                        "Complete: A group of friends ____ for an exam.",
                        listOf("study", "studies", "is studying", "studied"),
                        "is studying"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("cafeteria", "cafetería", "/ˌkæfəˈtɪriə/"),
                    VocabularySeed("lunch", "almuerzo", "/lʌntʃ/"),
                    VocabularySeed("classes", "clases", "/ˈklæsɪz/"),
                    VocabularySeed("upcoming", "próximo", "/ˈʌpkʌmɪŋ/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Office Work",
                unitId = 4,
                description = "Actividades laborales en desarrollo",
                content = "Emily is working at her office today. She is writing reports, answering emails and participating in an online meeting with clients from different countries.",
                grammar = "El presente continuo también se utiliza para describir actividades temporales que están ocurriendo actualmente.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Emily ____ reports.",
                        listOf("writes", "write", "is writing", "wrote"),
                        "is writing"
                    ),
                    QuestionSeed(
                        "Choose the correct option:",
                        listOf(
                            "She is answering emails",
                            "She answering emails",
                            "She answers emails now",
                            "She answered emails"
                        ),
                        "She is answering emails"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("reports", "informes", "/rɪˈpɔːrts/"),
                    VocabularySeed("emails", "correos electrónicos", "/ˈiːmeɪlz/"),
                    VocabularySeed("meeting", "reunión", "/ˈmiːtɪŋ/"),
                    VocabularySeed("clients", "clientes", "/ˈklaɪənts/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Sports Training",
                unitId = 4,
                description = "Acciones físicas que ocurren actualmente",
                content = "The basketball team is preparing for an important championship. The players are practicing new strategies and improving their teamwork during training sessions.",
                grammar = "Con sujetos plurales se utiliza 'are' + verbo terminado en '-ing'. Con sujetos singulares se utiliza 'is'.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The players ____ new strategies.",
                        listOf("practice", "practices", "are practicing", "practiced"),
                        "are practicing"
                    ),
                    QuestionSeed(
                        "Complete: The team ____ for a championship.",
                        listOf("prepare", "prepares", "is preparing", "prepared"),
                        "is preparing"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("championship", "campeonato", "/ˈtʃæmpiənʃɪp/"),
                    VocabularySeed("strategies", "estrategias", "/ˈstrætədʒiz/"),
                    VocabularySeed("teamwork", "trabajo en equipo", "/ˈtiːmwɜːrk/"),
                    VocabularySeed("training", "entrenamiento", "/ˈtreɪnɪŋ/")
                )
            ),

            ExerciseSeed(
                id = exerciseCounter++,
                name = "Present Continuous - Technology Project",
                unitId = 4,
                description = "Describir proyectos en progreso",
                content = "A group of engineering students is developing a mobile application. They are designing interfaces, testing features and solving technical problems as part of their final project.",
                grammar = "El presente continuo es útil para describir proyectos o procesos que se encuentran en desarrollo durante un período temporal.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The students ____ a mobile application.",
                        listOf("develop", "develops", "are developing", "developed"),
                        "are developing"
                    ),
                    QuestionSeed(
                        "Complete: They ____ technical problems.",
                        listOf("solve", "solves", "are solving", "solved"),
                        "are solving"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("engineering", "ingeniería", "/ˌendʒɪˈnɪrɪŋ/"),
                    VocabularySeed("application", "aplicación", "/ˌæplɪˈkeɪʃən/"),
                    VocabularySeed("interfaces", "interfaces", "/ˈɪntərfeɪsɪz/"),
                    VocabularySeed("features", "funcionalidades", "/ˈfiːtʃərz/")
                )
            ),

            // TEST UNIDAD 1 - TO BE

            ExerciseSeed(
                id = 31,
                name = "Test To Be - Personal Information",
                unitId = 1,
                description = "Evaluación de uso del verbo to be",
                content = "My name is Sarah. I am 20 years old and I am a university student. My brother is an engineer and my parents are teachers.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: I ____ 20 years old.",
                        listOf("is", "am", "are", "be"),
                        "am"
                    ),
                    QuestionSeed(
                        "Complete: My brother ____ an engineer.",
                        listOf("am", "are", "is", "be"),
                        "is"
                    ),
                    QuestionSeed(
                        "Complete: My parents ____ teachers.",
                        listOf("am", "is", "are", "be"),
                        "are"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("engineer", "ingeniero", "/ˌendʒɪˈnɪr/"),
                    VocabularySeed("parents", "padres", "/ˈperənts/")
                )
            ),

            ExerciseSeed(
                id = 32,
                name = "Test To Be - Friends",
                unitId = 1,
                description = "Evaluación final del verbo to be",
                content = "Tom and Lisa are friends. They are students at the same university. Tom is from Chile and Lisa is from Argentina.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Tom and Lisa ____ friends.",
                        listOf("is", "am", "are", "be"),
                        "are"
                    ),
                    QuestionSeed(
                        "Complete: Tom ____ from Chile.",
                        listOf("am", "are", "is", "be"),
                        "is"
                    ),
                    QuestionSeed(
                        "Complete: They ____ students.",
                        listOf("am", "is", "are", "be"),
                        "are"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("friends", "amigos", "/frendz/"),
                    VocabularySeed("university", "universidad", "/ˌjuːnɪˈvɜːrsəti/")
                )
            ),

            // TEST UNIDAD 2 - PRESENT SIMPLE
            ExerciseSeed(
                id = 33,
                name = "Test Present Simple - Work Routine",
                unitId = 2,
                description = "Evaluación de presente simple",
                content = "Carlos works in a bank. He starts work at 8 AM and helps customers every day. He enjoys his job and learns new skills every month.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Carlos ____ in a bank.",
                        listOf("work", "works", "working", "worked"),
                        "works"
                    ),
                    QuestionSeed(
                        "Complete: He ____ customers every day.",
                        listOf("help", "helps", "helping", "helped"),
                        "helps"
                    ),
                    QuestionSeed(
                        "Complete: He ____ new skills every month.",
                        listOf("learn", "learns", "learning", "learned"),
                        "learns"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("bank", "banco", "/bæŋk/"),
                    VocabularySeed("customers", "clientes", "/ˈkʌstəmərz/")
                )
            ),

            ExerciseSeed(
                id = 34,
                name = "Test Present Simple - Daily Activities",
                unitId = 2,
                description = "Evaluación de hábitos diarios",
                content = "Emma wakes up early every morning. She drinks coffee, reads the news and goes to work by bus.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Emma ____ up early.",
                        listOf("wake", "wakes", "waking", "woke"),
                        "wakes"
                    ),
                    QuestionSeed(
                        "Complete: She ____ coffee.",
                        listOf("drink", "drinks", "drinking", "drank"),
                        "drinks"
                    ),
                    QuestionSeed(
                        "Complete: She ____ to work by bus.",
                        listOf("go", "goes", "going", "went"),
                        "goes"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("coffee", "café", "/ˈkɔːfi/"),
                    VocabularySeed("news", "noticias", "/nuːz/")
                )
            ),

            // TEST UNIDAD 3 - DAILY ROUTINES

            ExerciseSeed(
                id = 35,
                name = "Test Daily Routines - Student Life",
                unitId = 3,
                description = "Evaluación de rutinas diarias",
                content = "Michael is a university student. He attends classes every morning, studies in the library during the afternoon and exercises at the gym in the evening.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: He ____ classes every morning.",
                        listOf("attend", "attends", "attending", "attended"),
                        "attends"
                    ),
                    QuestionSeed(
                        "Complete: He ____ in the library.",
                        listOf("study", "studies", "studying", "studied"),
                        "studies"
                    ),
                    QuestionSeed(
                        "Where does Michael exercise?",
                        listOf("At home", "At the gym", "At the library", "At school"),
                        "At the gym"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("library", "biblioteca", "/ˈlaɪbreri/"),
                    VocabularySeed("gym", "gimnasio", "/dʒɪm/")
                )
            ),

            ExerciseSeed(
                id = 36,
                name = "Test Daily Routines - Family Activities",
                unitId = 3,
                description = "Evaluación de hábitos familiares",
                content = "The Rodriguez family has dinner together every night. After dinner, they watch television and talk about their day before going to bed.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The family ____ dinner together.",
                        listOf("have", "has", "having", "had"),
                        "has"
                    ),
                    QuestionSeed(
                        "Complete: They ____ television after dinner.",
                        listOf("watch", "watches", "watching", "watched"),
                        "watch"
                    ),
                    QuestionSeed(
                        "What do they do before going to bed?",
                        listOf(
                            "Study English",
                            "Go shopping",
                            "Talk about their day",
                            "Exercise"
                        ),
                        "Talk about their day"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("dinner", "cena", "/ˈdɪnər/"),
                    VocabularySeed("television", "televisión", "/ˈtelɪvɪʒən/")
                )
            ),

            // TEST UNIDAD 4 - PRESENT CONTINUOUS

            ExerciseSeed(
                id = 37,
                name = "Test Present Continuous - Classroom Activity",
                unitId = 4,
                description = "Evaluación de acciones en progreso",
                content = "The students are working on a group project. Some are researching information online while others are preparing a presentation.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The students ____ on a project.",
                        listOf("work", "works", "are working", "worked"),
                        "are working"
                    ),
                    QuestionSeed(
                        "Complete: Some students ____ information online.",
                        listOf("research", "researches", "are researching", "researched"),
                        "are researching"
                    ),
                    QuestionSeed(
                        "Others ____ a presentation.",
                        listOf("prepare", "prepares", "are preparing", "prepared"),
                        "are preparing"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("project", "proyecto", "/ˈprɑːdʒekt/"),
                    VocabularySeed("presentation", "presentación", "/ˌprezənˈteɪʃən/")
                )
            ),

            ExerciseSeed(
                id = 38,
                name = "Test Present Continuous - City Park",
                unitId = 4,
                description = "Evaluación del presente continuo",
                content = "Many people are spending time in the city park today. Children are playing, families are having picnics and some people are riding bicycles.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Children ____ in the park.",
                        listOf("play", "plays", "are playing", "played"),
                        "are playing"
                    ),
                    QuestionSeed(
                        "Complete: Families ____ picnics.",
                        listOf("have", "has", "are having", "had"),
                        "are having"
                    ),
                    QuestionSeed(
                        "Some people ____ bicycles.",
                        listOf("ride", "rides", "are riding", "rode"),
                        "are riding"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("park", "parque", "/pɑːrk/"),
                    VocabularySeed("picnics", "picnics", "/ˈpɪknɪks/"),
                    VocabularySeed("bicycles", "bicicletas", "/ˈbaɪsɪkəlz/")
                )
            ),
            // TEST UNIDAD 5 - PAST SIMPLE

            ExerciseSeed(
                id = 39,
                name = "Test Past Simple - Vacation Trip",
                unitId = 5,
                description = "Evaluación del pasado simple",
                content = "Last summer, Anna traveled to Peru with her family. They visited historical sites, took many photographs and enjoyed local food during their trip.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Anna ____ to Peru last summer.",
                        listOf("travel", "travels", "traveled", "traveling"),
                        "traveled"
                    ),
                    QuestionSeed(
                        "Complete: They ____ historical sites.",
                        listOf("visit", "visits", "visited", "visiting"),
                        "visited"
                    ),
                    QuestionSeed(
                        "What did they enjoy?",
                        listOf(
                            "The weather",
                            "Local food",
                            "Their hotel",
                            "The airport"
                        ),
                        "Local food"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("traveled", "viajó", "/ˈtrævəld/"),
                    VocabularySeed("historical", "histórico", "/hɪˈstɔːrɪkəl/"),
                    VocabularySeed("photographs", "fotografías", "/ˈfoʊtəɡræfs/")
                )
            ),

            ExerciseSeed(
                id = 40,
                name = "Test Past Simple - School Event",
                unitId = 5,
                description = "Evaluación de eventos pasados",
                content = "Last month, the students organized a science fair. They prepared projects, presented their ideas and received positive feedback from teachers.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The students ____ a science fair.",
                        listOf("organize", "organized", "organizes", "organizing"),
                        "organized"
                    ),
                    QuestionSeed(
                        "Complete: They ____ their projects.",
                        listOf("prepare", "prepared", "prepares", "preparing"),
                        "prepared"
                    ),
                    QuestionSeed(
                        "Who gave feedback?",
                        listOf(
                            "Parents",
                            "Friends",
                            "Teachers",
                            "Students"
                        ),
                        "Teachers"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("science fair", "feria científica", "/ˈsaɪəns fer/"),
                    VocabularySeed("projects", "proyectos", "/ˈprɑːdʒekts/"),
                    VocabularySeed("feedback", "retroalimentación", "/ˈfiːdbæk/")
                )
            ),

            // TEST UNIDAD 6 - FUTURE FORMS

            ExerciseSeed(
                id = 41,
                name = "Test Future Forms - University Plans",
                unitId = 6,
                description = "Evaluación de formas futuras",
                content = "Laura is going to start a new semester next month. She is going to take advanced programming courses and she believes she will improve her technical skills.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: Laura ____ start a new semester.",
                        listOf(
                            "will",
                            "is going to",
                            "goes to",
                            "started"
                        ),
                        "is going to"
                    ),
                    QuestionSeed(
                        "Complete: She ____ advanced programming courses.",
                        listOf(
                            "is going to take",
                            "takes",
                            "took",
                            "taking"
                        ),
                        "is going to take"
                    ),
                    QuestionSeed(
                        "She thinks she ____ her skills.",
                        listOf(
                            "improves",
                            "improved",
                            "will improve",
                            "improving"
                        ),
                        "will improve"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("semester", "semestre", "/səˈmestər/"),
                    VocabularySeed("advanced", "avanzado", "/ədˈvænst/"),
                    VocabularySeed("skills", "habilidades", "/skɪlz/")
                )
            ),

            ExerciseSeed(
                id = 42,
                name = "Test Future Forms - Smart City Project",
                unitId = 6,
                description = "Evaluación final de futuro",
                content = "The city is going to implement a smart transportation system next year. Experts believe it will reduce traffic and improve public transportation services.",
                grammar = "Evaluación de la unidad.",
                questions = listOf(
                    QuestionSeed(
                        "Complete: The city ____ implement a new system.",
                        listOf(
                            "is going to",
                            "will implementing",
                            "implemented",
                            "implements"
                        ),
                        "is going to"
                    ),
                    QuestionSeed(
                        "Experts believe it ____ traffic.",
                        listOf(
                            "reduces",
                            "reduced",
                            "will reduce",
                            "reducing"
                        ),
                        "will reduce"
                    ),
                    QuestionSeed(
                        "What will improve?",
                        listOf(
                            "Schools",
                            "Public transportation services",
                            "Hospitals",
                            "Universities"
                        ),
                        "Public transportation services"
                    )
                ),
                vocabulary = listOf(
                    VocabularySeed("transportation", "transporte", "/ˌtrænspərˈteɪʃən/"),
                    VocabularySeed("traffic", "tráfico", "/ˈtræfɪk/"),
                    VocabularySeed("services", "servicios", "/ˈsɜːrvɪsɪz/")
                )
            ),
        )
    }

    fun getTestSeeds(): List<TestSeed> {
        return listOf(

            TestSeed(
                name = "Test Unidad 1 - To Be",
                unitId = 1,
                description = "Evaluación del verbo to be",
                exerciseIds = listOf(31, 32)
            ),

            TestSeed(
                name = "Test Unidad 2 - Present Simple",
                unitId = 2,
                description = "Evaluación del presente simple",
                exerciseIds = listOf(33, 34)
            ),

            TestSeed(
                name = "Test Unidad 3 - Daily Routines",
                unitId = 3,
                description = "Evaluación de rutinas y hábitos",
                exerciseIds = listOf(35, 36)
            ),

            TestSeed(
                name = "Test Unidad 4 - Present Continuous",
                unitId = 4,
                description = "Evaluación del presente continuo",
                exerciseIds = listOf(37, 38)
            ),

            TestSeed(
                name = "Test Unidad 5 - Past Simple",
                unitId = 5,
                description = "Evaluación del pasado simple",
                exerciseIds = listOf(39, 40)
            ),

            TestSeed(
                name = "Test Unidad 6 - Future Forms",
                unitId = 6,
                description = "Evaluación de formas futuras",
                exerciseIds = listOf(41, 42)
            )
        )
    }

    fun getCompletedExerciseSeeds(): List<CompletedExerciseSeed> {
        return listOf(

            // Student 1 (id=3) - Completó Unidad 1
            CompletedExerciseSeed(userId = 3, exerciseId = 1),
            CompletedExerciseSeed(userId = 3, exerciseId = 2),
            CompletedExerciseSeed(userId = 3, exerciseId = 3),
            CompletedExerciseSeed(userId = 3, exerciseId = 4),

            // Student 2 (id=4) - Completó Unidad 1 y parte de Unidad 2
            CompletedExerciseSeed(userId = 4, exerciseId = 1),
            CompletedExerciseSeed(userId = 4, exerciseId = 2),
            CompletedExerciseSeed(userId = 4, exerciseId = 3),
            CompletedExerciseSeed(userId = 4, exerciseId = 4),
            CompletedExerciseSeed(userId = 4, exerciseId = 8),
            CompletedExerciseSeed(userId = 4, exerciseId = 9),

            // Student 3 (id=5) - Completó Unidad 1 y Unidad 2
            CompletedExerciseSeed(userId = 5, exerciseId = 1),
            CompletedExerciseSeed(userId = 5, exerciseId = 2),
            CompletedExerciseSeed(userId = 5, exerciseId = 3),
            CompletedExerciseSeed(userId = 5, exerciseId = 4),
            CompletedExerciseSeed(userId = 5, exerciseId = 8),
            CompletedExerciseSeed(userId = 5, exerciseId = 9),
            CompletedExerciseSeed(userId = 5, exerciseId = 10),
            CompletedExerciseSeed(userId = 5, exerciseId = 11),

            // Student 4 (id=6) - Llegó hasta Unidad 4
            CompletedExerciseSeed(userId = 6, exerciseId = 1),
            CompletedExerciseSeed(userId = 6, exerciseId = 2),
            CompletedExerciseSeed(userId = 6, exerciseId = 3),
            CompletedExerciseSeed(userId = 6, exerciseId = 4),

            CompletedExerciseSeed(userId = 6, exerciseId = 8),
            CompletedExerciseSeed(userId = 6, exerciseId = 9),
            CompletedExerciseSeed(userId = 6, exerciseId = 10),
            CompletedExerciseSeed(userId = 6, exerciseId = 11),

            CompletedExerciseSeed(userId = 6, exerciseId = 12),
            CompletedExerciseSeed(userId = 6, exerciseId = 13),
            CompletedExerciseSeed(userId = 6, exerciseId = 14),
            CompletedExerciseSeed(userId = 6, exerciseId = 15),

            CompletedExerciseSeed(userId = 6, exerciseId = 16),
            CompletedExerciseSeed(userId = 6, exerciseId = 17),

            // Student 5 (id=7) - Completó todas las unidades
            *(
                    (1..27).map {
                        CompletedExerciseSeed(userId = 7, exerciseId = it)
                    }.toTypedArray()
                    )
        )
    }

    fun getCompletedUnitSeeds(): List<CompletedUnitSeed> {
        return listOf(

            // Student 1
            CompletedUnitSeed(userId = 3, unitId = 1),

            // Student 2
            CompletedUnitSeed(userId = 4, unitId = 1),

            // Student 3
            CompletedUnitSeed(userId = 5, unitId = 1),
            CompletedUnitSeed(userId = 5, unitId = 2),

            // Student 4
            CompletedUnitSeed(userId = 6, unitId = 1),
            CompletedUnitSeed(userId = 6, unitId = 2),
            CompletedUnitSeed(userId = 6, unitId = 3),

            // Student 5
            CompletedUnitSeed(userId = 7, unitId = 1),
            CompletedUnitSeed(userId = 7, unitId = 2),
            CompletedUnitSeed(userId = 7, unitId = 3),
            CompletedUnitSeed(userId = 7, unitId = 4),
            CompletedUnitSeed(userId = 7, unitId = 5),
            CompletedUnitSeed(userId = 7, unitId = 6)
        )
    }

    fun getTestCompletedSeeds(): List<TestCompletedSeed> {
        return listOf(

            // Student 1
            TestCompletedSeed(userId = 3, testId = 1, score = 82),

            // Student 2
            TestCompletedSeed(userId = 4, testId = 1, score = 90),

            // Student 3
            TestCompletedSeed(userId = 5, testId = 1, score = 88),
            TestCompletedSeed(userId = 5, testId = 2, score = 80),

            // Student 4
            TestCompletedSeed(userId = 6, testId = 1, score = 85),
            TestCompletedSeed(userId = 6, testId = 2, score = 92),
            TestCompletedSeed(userId = 6, testId = 3, score = 87),

            // Student 5
            TestCompletedSeed(userId = 7, testId = 1, score = 95),
            TestCompletedSeed(userId = 7, testId = 2, score = 93),
            TestCompletedSeed(userId = 7, testId = 3, score = 91),
            TestCompletedSeed(userId = 7, testId = 4, score = 94),
            TestCompletedSeed(userId = 7, testId = 5, score = 89),
            TestCompletedSeed(userId = 7, testId = 6, score = 96)
        )
    }

    fun getSessionLogSeeds(): List<SessionLogSeed> {
        val now = LocalDateTime.now()

        return listOf(

            // Student 1
            SessionLogSeed(
                userId = 3,
                loginAt = now.minusDays(7),
                logoutAt = now.minusDays(7).plusHours(1),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 3,
                loginAt = now.minusDays(3),
                logoutAt = now.minusDays(3).plusMinutes(45),
                endReason = SessionEndReason.LOGOUT
            ),

            // Student 2
            SessionLogSeed(
                userId = 4,
                loginAt = now.minusDays(5),
                logoutAt = now.minusDays(5).plusHours(2),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 4,
                loginAt = now.minusDays(1),
                logoutAt = now.minusDays(1).plusMinutes(50),
                endReason = SessionEndReason.LOGOUT
            ),

            // Student 3
            SessionLogSeed(
                userId = 5,
                loginAt = now.minusDays(4),
                logoutAt = now.minusDays(4).plusHours(1),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 5,
                loginAt = now.minusDays(2),
                logoutAt = now.minusDays(2).plusHours(1).plusMinutes(20),
                endReason = SessionEndReason.LOGOUT
            ),

            // Student 4
            SessionLogSeed(
                userId = 6,
                loginAt = now.minusDays(6),
                logoutAt = now.minusDays(6).plusHours(2),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 6,
                loginAt = now.minusDays(1),
                logoutAt = now.minusDays(1).plusHours(1).plusMinutes(40),
                endReason = SessionEndReason.LOGOUT
            ),

            // Student 5
            SessionLogSeed(
                userId = 7,
                loginAt = now.minusDays(7),
                logoutAt = now.minusDays(7).plusHours(3),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 7,
                loginAt = now.minusHours(12),
                logoutAt = now.minusHours(10),
                endReason = SessionEndReason.LOGOUT
            )
        )
    }
}

fun insertSeedData() {
    transaction {
        val unitSeed = SeedDataProvider.getUnitsSeeds()

        unitSeed.forEach { unit ->

            Units.insert {
                it[name] = unit.name
                it[difficulty] = unit.difficulty
                it[description] = unit.description
                it[orderUnit] = unit.orderUnit
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            }[Units.id]
        }

        val seeds = SeedDataProvider.getExerciseSeeds()
        var exerciseOrder = 1

        seeds.forEach { seed ->
            val exId = Exercises.insert {
                it[unitId] = seed.unitId
                it[name] = seed.name
                it[description] = seed.description
                it[orderExercise] = exerciseOrder++
                it[isActive] = true
            }[Exercises.id]

            val ecId = ExerciseContent.insert {
                it[exerciseId] = exId.value
                it[contentType] = ContentType.READING
                it[textContent] = seed.content
                it[grammarExplanation] = seed.grammar
                it[audioUrl] = null
            }[ExerciseContent.id]

            seed.vocabulary.forEach { vocab ->
                val wordId = Words.insert {
                    it[english] = vocab.english.trim()
                    it[spanish] = vocab.spanish.trim()
                    it[phonetic] = vocab.phonetic.trim()
                    it[description] = null
                    it[isActive] = true
                }[Words.id]

                ExerciseWords.insert {
                    it[exerciseId] = exId.value
                    it[this.wordId] = wordId.value
                }
            }

            var questionOrder = 1
            seed.questions.forEach { question ->
                val qId = Questions.insert {
                    it[exerciseContentId] = ecId.value
                    it[questionText] = question.text
                    it[orderQuestion] = questionOrder++
                    it[isActive] = true
                }[Questions.id]

                question.alternatives.forEach { alternative ->
                    Alternatives.insert {
                        it[questionId] = qId.value
                        it[text] = alternative.trim()
                        it[isCorrect] = (alternative.trim() == question.correct.trim())
                    }
                }
            }
        }

        val testSeeds = SeedDataProvider.getTestSeeds()
        testSeeds.forEach { testSeed ->
            val testId = Tests.insert {
                it[unitId] = testSeed.unitId
                it[name] = testSeed.name
                it[description] = testSeed.description
                it[isActive] = true
            }[Tests.id]

            testSeed.exerciseIds.forEach { exerciseId ->
                TestExercises.insert {
                    it[this.testId] = testId.value
                    it[this.exerciseId] = exerciseId
                }
            }
        }

        val completedExercises = SeedDataProvider.getCompletedExerciseSeeds()
        completedExercises.forEach { completed ->
            ExerciseCompleted.insert {
                it[userId] = completed.userId
                it[exerciseId] = completed.exerciseId
                it[completionDate] = LocalDateTime.now()
            }
        }

        val completedUnits = SeedDataProvider.getCompletedUnitSeeds()
        completedUnits.forEach { completed ->
            UnitsCompleted.insert {
                it[userId] = completed.userId
                it[unitId] = completed.unitId
                it[completionDate] = LocalDateTime.now()
            }
        }

        val completedTests = SeedDataProvider.getTestCompletedSeeds()
        completedTests.forEach { completed ->
            TestCompleted.insert {
                it[userId] = completed.userId
                it[testId] = completed.testId
                it[score] = completed.score
                it[completionDate] = LocalDateTime.now()
            }
        }

        val sessionLogs = SeedDataProvider.getSessionLogSeeds()
        sessionLogs.forEach { session ->
            UserSessionLogs.insert {
                it[userId] = session.userId
                it[loginAt] = session.loginAt
                it[logoutAt] = session.logoutAt
                it[endReason] = session.endReason
                it[createdAt] = LocalDateTime.now()
            }
        }
    }
}


