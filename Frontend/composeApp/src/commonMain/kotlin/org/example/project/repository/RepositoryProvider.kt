import org.example.project.repository.levelRepository.KtorLevelRepository
import org.example.project.network.createHttpClient
import org.example.project.repository.dialogsRepository.KtorDialogsRepository

object RepositoryProvider {
    private val httpClient = createHttpClient()
    private const val baseUrl = "http://146.83.198.35:1667"
    val levelRepository by lazy { KtorLevelRepository(httpClient, baseUrl) }
    val dialogsRepository by lazy { KtorDialogsRepository(
        httpClient, baseUrl,
        levelsRepo = levelRepository
    ) }
}