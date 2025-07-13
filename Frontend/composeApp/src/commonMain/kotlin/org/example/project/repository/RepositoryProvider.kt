import org.example.project.repository.levelRepository.KtorLevelRepository
import org.example.project.network.createHttpClient

object RepositoryProvider {
    private val httpClient = createHttpClient()
    private const val baseUrl = "http://10.0.2.2:443"
    val levelRepository by lazy { KtorLevelRepository(httpClient, baseUrl) }
}