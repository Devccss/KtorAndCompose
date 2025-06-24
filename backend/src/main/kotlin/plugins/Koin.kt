package com.example.plugins
import repositories.LevelRepository
import services.LevelService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

// En backend/src/main/kotlin/plugins/Koin.kt

val levelModule = module {
    single { LevelRepository() }
    single { LevelService(get()) } // get() inyecta automáticamente el Repository
}


// 2. Configura Koin en tu aplicación Ktor
fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            levelModule,

        )
    }

}