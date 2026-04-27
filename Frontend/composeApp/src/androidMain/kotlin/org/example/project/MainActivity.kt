package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.runBlocking
import org.example.project.network.SessionLogTracker
import org.example.project.network.UserSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            runBlocking {
                SessionLogTracker.closeForAppClosed(UserSession.idUser)
            }
        }
        super.onDestroy()
    }
}
