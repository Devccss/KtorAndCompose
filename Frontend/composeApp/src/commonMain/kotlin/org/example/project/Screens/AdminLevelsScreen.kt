import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

class AdminLevelsScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        // Obtén el ViewModel de Voyager
        val viewModel = rememberScreenModel { LevelsViewModel(repository = KtorLevelRepository(
            httpClient = HttpClient {
                install(ContentNegotiation) {
                    json()
                }
            }
        ))}

        val state by viewModel.state.collectAsState()

        Scaffold(
            // ... resto de tu implementación UI
        ) { padding ->
            when {
                state.isLoading -> LoadingIndicator()
                state.error != null -> ErrorMessage(state.error!!)
                else -> LevelsList( levels = state.levels,
                    modifier = Modifier.padding(padding),
                    onEdit = TODO(),
                    onDelete = TODO()
                )
            }
        }
    }

}

@Composable
fun LevelsList(
    levels: List<Level>,
    onEdit: (Level) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(levels) { level ->
            LevelCard(
                level = level,
                onEdit = { onEdit(level) },
                onDelete = { onDelete(level.id) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun LevelCard(
    level: Level,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${level.position}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(0.1f)
                )
                Text(
                    text = level.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Eliminar")
                }
            }
        }
    }
}