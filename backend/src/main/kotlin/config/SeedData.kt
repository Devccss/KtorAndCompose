package config

import models.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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

object SeedDataProvider {

    private var exerciseCounter = 1

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
                        listOf("She are a teacher", "She is a teacher", "She am a teacher", "She be a teacher"),
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
                        listOf("Are you ready?", "You are ready?", "Ready are you?", "Are ready you?"),
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
                        listOf("She is intelligents", "She are intelligent", "She is intelligent", "She intelligent"),
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
                        listOf("She am an actress", "She is an actress", "She are actress", "She be an actress"),
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
                        listOf("He is at the beach", "He am at beach", "He is beach", "He on the beach"),
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
                        listOf("The books is new", "The books are new", "The books am new", "The book are new"),
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
            )
        )
    }

    fun getTestSeeds(): List<TestSeed> {
        return listOf(
            TestSeed(
                name = "Test Unidad 1 - To Be",
                unitId = 1,
                description = "Evaluación completa del verbo 'to be'",
                exerciseIds = (1..10).toList()
            )
        )
    }

    fun getCompletedExerciseSeeds(): List<CompletedExerciseSeed> {
        return listOf(
            CompletedExerciseSeed(userId = 2, exerciseId = 1),
            CompletedExerciseSeed(userId = 2, exerciseId = 2),
            CompletedExerciseSeed(userId = 2, exerciseId = 3)
        )
    }

    fun getCompletedUnitSeeds(): List<CompletedUnitSeed> {
        return listOf(
            CompletedUnitSeed(userId = 2, unitId = 1)
        )
    }

    fun getTestCompletedSeeds(): List<TestCompletedSeed> {
        return listOf(
            TestCompletedSeed(userId = 2, testId = 1, score = 85)
        )
    }

    fun getSessionLogSeeds(): List<SessionLogSeed> {
        val now = LocalDateTime.now()
        return listOf(
            SessionLogSeed(
                userId = 2,
                loginAt = now.minusDays(7),
                logoutAt = now.minusDays(7).plusHours(2),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 2,
                loginAt = now.minusDays(5),
                logoutAt = now.minusDays(5).plusHours(1).plusMinutes(30),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 2,
                loginAt = now.minusDays(3),
                logoutAt = now.minusDays(3).plusHours(3),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 2,
                loginAt = now.minusDays(1),
                logoutAt = now.minusDays(1).plusHours(1).plusMinutes(45),
                endReason = SessionEndReason.LOGOUT
            ),
            SessionLogSeed(
                userId = 2,
                loginAt = now.minusHours(3),
                logoutAt = null,
                endReason = null
            )
        )
    }
}

fun insertSeedData() {
    transaction {
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


