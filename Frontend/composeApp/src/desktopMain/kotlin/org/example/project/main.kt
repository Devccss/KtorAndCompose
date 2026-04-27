package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import org.example.project.network.SessionLogTracker
import org.example.project.network.UserSession

fun main() = application {
    Window(
        onCloseRequest = {
            runBlocking {
                SessionLogTracker.closeForAppClosed(UserSession.idUser)
            }
            exitApplication()
        },
        title = "Frontend",
    ) {
        App()
    }
}