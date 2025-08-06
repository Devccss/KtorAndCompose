import org.example.project.repository.levelRepository.KtorLevelRepository
import org.example.project.network.createHttpClient
import org.example.project.repository.ParticipantsRepository
import org.example.project.repository.PhraseRepository
import org.example.project.repository.PhraseWordRepository
import org.example.project.repository.UsersRepository.UserRepo
import org.example.project.repository.WordRepository
import org.example.project.repository.dialogsRepository.KtorDialogsRepository

object RepositoryProvider {
    private val httpClient = createHttpClient()
    private const val baseUrl = "http://146.83.198.35:1667"
    //private const val baseUrl = "http://10.0.2.2:8080"
    //private const val baseUrl = "http://146.83.194.168:8080"
    val levelRepository by lazy { KtorLevelRepository(httpClient, baseUrl) }
    val dialogsRepository by lazy { KtorDialogsRepository(
        httpClient, baseUrl,
        levelsRepo = levelRepository
    ) }
    val usersRepository by lazy { UserRepo(
        httpClient, baseUrl
    ) }
    val participantsRepository by lazy { ParticipantsRepository(httpClient, baseUrl) }
    val phrasesRepository by lazy { PhraseRepository(
        httpClient, baseUrl
    ) }
    val wordsRepository by lazy { WordRepository(
        httpClient, baseUrl
    ) }
    val phraseWordRepository by lazy { PhraseWordRepository(
        httpClient, baseUrl
    ) }
}