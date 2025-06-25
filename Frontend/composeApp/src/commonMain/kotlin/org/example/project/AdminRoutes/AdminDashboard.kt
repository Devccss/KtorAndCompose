import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.screens.AdminLevelsScreen


class AdminDashboard : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        AdminHomeContent()
    }
}

@Composable
fun AdminHomeContent() {
    val navigator = LocalNavigator.currentOrThrow
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            AdminNavBar(
                currentDestination = navigator.lastItem.key,
                onNavigate = { destination ->
                    try {
                        when (destination) {
                            "users" -> navigator.push(AdminUsersScreen())
                            "levels" -> navigator.push(AdminLevelsScreen())
                            else -> navigator.popUntilRoot()
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de navegación: ${e.message}"
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (errorMessage != null) {
                ErrorMessage(errorMessage!!)
            } else {
                // Llama directamente al contenido sin try-catch
                navigator.saveableState("adminContent") {
                    AdminDashboardScreen().Content()
                }
            }
        }
    }
}

@Composable
fun AdminNavBar(
    currentDestination: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icons.Default.Person },
            label = { Text("Usuarios") },
            selected = currentDestination.contains("AdminUsersScreen"),
            onClick = { onNavigate("users") }
        )
        NavigationBarItem(
            icon = { Icons.Default.Balance },
            label = { Text("Niveles") },
            selected = currentDestination.contains("org.example.project.Screens.AdminLevelsScreen"),
            onClick = { onNavigate("levels") }
        )
        NavigationBarItem(
            icon = { Icons.Default.Ballot},
            label = { Text("Dashboard") },
            selected = currentDestination.contains("AdminDashboardScreen"),
            onClick = { onNavigate("dashboard") }
        )
    }
}


@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}