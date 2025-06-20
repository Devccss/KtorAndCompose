import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey

class AdminDashboardScreen : Screen {
    override val key = uniqueScreenKey
    @Composable
    override fun Content() { /* ... */ }
}