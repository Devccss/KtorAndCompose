package config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*


fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader("MyCustomHeader")
        allowHost("localhost", schemes = listOf("http", "https"))
        allowHost("127.0.0.1", schemes = listOf("http", "https"))
        allowHost("10.0.2.2", schemes = listOf("http", "https")) // para emuladores Android
        allowHost("192.168.1.6", schemes = listOf("http", "https"))
        allowOrigins { origin ->
            // Allow all origins for development, but you should restrict this in production
            true
        }
    }
}
