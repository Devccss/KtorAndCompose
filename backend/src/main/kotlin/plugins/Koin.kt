package com.example.plugins
import com.example.services.DialogService
import repositories.LevelRepository
import services.LevelService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import repositories.DialogRepository

// En backend/src/main/kotlin/plugins/Koin.kt


val repositoryModule = module {
    single { LevelRepository(get()) }
    single { DialogRepository() }
}

val serviceModule = module {
    single { LevelService(get()) }
    single { DialogService(get()) }
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