package com.example.plugins
import com.example.repositories.DialogParticipantsRepository
import com.example.repositories.PhraseRepository
import com.example.repositories.PhraseWordRepository
import com.example.repositories.UsersRepository
import com.example.repositories.WordRepository
import com.example.services.DialogParticipantsService
import com.example.services.DialogService
import com.example.services.PhraseService
import com.example.services.PhraseWordService
import com.example.services.UserService
import com.example.services.WordService
import repositories.LevelRepository
import services.LevelService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.scope.get
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import repositories.DialogRepository

// En backend/src/main/kotlin/plugins/Koin.kt


val repositoryModule = module {
    single { LevelRepository(get()) }
    single { DialogRepository() }
    single { DialogParticipantsRepository() }
    single { PhraseRepository() }
    single { WordRepository() }
    single { PhraseWordRepository() }
    single { UsersRepository() }
}

val serviceModule = module {
    single { LevelService(get()) }
    single { DialogService(get()) }
    single { DialogParticipantsService(get()) }
    single { PhraseService(get()) }
    single { WordService(get()) }
    single { PhraseWordService(get()) }
    single { UserService(get()) }
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