package com.example.plugins
import com.example.services.ContentExerciseService
import com.example.services.ContentWordService
import com.example.services.ExerciseOnHoldService
import com.example.services.ExerciseService
import com.example.services.NotificationsService
import com.example.services.QuestionService
import com.example.services.TestExerciseService
import com.example.services.TestService
import com.example.services.UnitService
import com.example.services.UserService
import com.example.services.WordService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import repositories.ContentExerciseRepository
import repositories.ContentWordRepository
import repositories.ExerciseOnHoldRepository
import repositories.ExerciseRepository
import repositories.NotificationsRepository
import repositories.QuestionRepository
import repositories.TestExerciseRepository
import repositories.TestRepository
import repositories.UnitRepository
import repositories.UsersRepository
import repositories.WordRepository


val repositoryModule = module {

    single { UsersRepository() }
    single { UnitRepository() }
    single { WordRepository() }
    single { TestRepository() }
    single { TestExerciseRepository() }
    single { QuestionRepository() }
    single { NotificationsRepository() }
    single { ExerciseRepository() }
    single { ExerciseOnHoldRepository() }
    single { ContentWordRepository()}
    single { ContentExerciseRepository() }

}

val serviceModule = module {

    single { UserService(get()) }
    single { UnitService(get()) }
    single { WordService(get()) }
    single { TestService(get()) }
    single { TestExerciseService(get()) }
    single { QuestionService(get()) }
    single { NotificationsService(get()) }
    single { ExerciseService(get()) }
    single { ExerciseOnHoldService(get()) }
    single { ContentWordService(get()) }
    single { ContentExerciseService(get()) }
}

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            repositoryModule,
            serviceModule
        )
    }
}