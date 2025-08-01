package org.example.project.screens.studentScreens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen


class StudentDashboard : Screen {
    @Composable
    override fun Content() {
        Text("Student Dashboard")
    }
}