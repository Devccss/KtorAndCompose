package com.example.plugins
import com.example.repositories.ExerciseContentRepository
import com.example.repositories.WelcomeTestRepo
import com.example.services.AiQuestionGenerationService
import com.example.services.ExerciseContentService
import com.example.services.ExerciseWordService
import com.example.services.ExerciseService
import com.example.services.NotificationsService
import com.example.services.QuestionAIClientService
import com.example.services.QuestionService
import com.example.services.TestExerciseService
import com.example.services.TestService
import services.UnitService
import com.example.services.UserService
import com.example.services.UserSessionLogsService
import com.example.services.WelcomeTestService
import com.example.services.WordService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import repositories.ExerciseWordsRepository
import repositories.ExerciseRepository
import repositories.NotificationsRepository
import repositories.QuestionRepository
import repositories.TestExerciseRepository
import repositories.TestRepository
import repositories.UnitRepository
import repositories.UsersRepository
import repositories.UserSessionLogsRepository
import repositories.WordRepository


val repositoryModule = module {

    single { UsersRepository() }
    single { UserSessionLogsRepository() }
    single { UnitRepository() }
    single { WordRepository() }
    single { TestRepository() }
    single { TestExerciseRepository() }
    single { QuestionRepository() }
    single { NotificationsRepository() }
    single { ExerciseRepository() }
    single { ExerciseContentRepository() }
    single { ExerciseWordsRepository()}
    single { WelcomeTestRepo() }


}

fun serviceModule(stringApiKey: String, baseUrlIa: String, longTimeoutMs: Long) = module {

    single { UserService(get()) }
    single { UserSessionLogsService(get()) }
    single { UnitService(get()) }
    single { WordService(get()) }
    single { TestService(get()) }
    single { TestExerciseService(get()) }
    single { QuestionService(get()) }
    single { NotificationsService(get()) }
    single { ExerciseService(get()) }
    single { ExerciseContentService(get()) }
    single { ExerciseWordService(get()) }
    single { WelcomeTestService(get()) }
    single { QuestionAIClientService(
        baseUrl = baseUrlIa,
        apiKey = stringApiKey,
        timeoutMs = longTimeoutMs
    ) }
    single { AiQuestionGenerationService(get(), get(), get()) }

}

fun Application.configureKoin() {
    val apiKey = environment.config.property("ai.python.apiKey").getString()
    val timeoutMs = environment.config
        .propertyOrNull("ai.python.timeoutMs")
        ?.getString()
        ?.toLong()
        ?: 30000L
    val baseurlIa = environment.config.property("ai.python.baseUrl").getString()

    install(Koin) {
        slf4jLogger()
        modules(
            repositoryModule,
            serviceModule(apiKey,baseurlIa, timeoutMs)
        )
    }
}
