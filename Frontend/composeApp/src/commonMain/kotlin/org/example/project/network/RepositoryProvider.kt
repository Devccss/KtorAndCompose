package org.example.project.network

import io.ktor.client.HttpClient
import org.example.project.repository.ExerciseRepo
import org.example.project.repository.QuestionsRepo
import org.example.project.repository.TestRepo
import org.example.project.repository.UnitRepo
import org.example.project.repository.UserRepo
import org.example.project.repository.WelcomeTestRepo
import org.example.project.repository.WordRepository


object RepositoryProvider {
    private var initialized = false

    // Repositorios respaldados por nullable para evitar lateinit exceptions
    private var _userRepo: UserRepo? = null
    val userRepo: UserRepo
        get() = _userRepo ?: throw IllegalStateException(
            "RepositoryProvider.userRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )

    private var _unitRepo: UnitRepo? = null
    val unitRepo: UnitRepo
        get() = _unitRepo ?: throw IllegalStateException(
            "RepositoryProvider.unitRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )

    private var _exerciseRepo: ExerciseRepo? = null
    val exerciseRepo: ExerciseRepo
        get() = _exerciseRepo ?: throw IllegalStateException(
            "RepositoryProvider.exerciseRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )

    private var _questionRepo: QuestionsRepo? = null
    val questionRepo: QuestionsRepo
        get() = _questionRepo ?: throw IllegalStateException(
            "RepositoryProvider.questionRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )

    private var _testRepo: TestRepo? = null
    val testRepo: TestRepo
        get() = _testRepo ?: throw IllegalStateException(
            "RepositoryProvider.testRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )

    private var _wordRepo: WordRepository? = null
    val wordRepo: WordRepository
        get() = _wordRepo ?: throw IllegalStateException(
            "RepositoryProvider.wordRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )

    private var _welcomeTestRepo: WelcomeTestRepo? = null
    val welcomeTestRepo: WelcomeTestRepo
        get() = _welcomeTestRepo ?: throw IllegalStateException(
            "RepositoryProvider.welcomeTestRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )


    fun init(httpClient: HttpClient, baseUrl: String) {
        if (initialized) return

        _userRepo = UserRepo(httpClient, baseUrl)
        _unitRepo = UnitRepo(httpClient, baseUrl)
        _exerciseRepo = ExerciseRepo(httpClient, baseUrl)
        _questionRepo = QuestionsRepo(httpClient, baseUrl)
        _testRepo = TestRepo(httpClient, baseUrl)
        _wordRepo = WordRepository(httpClient, baseUrl)
        _welcomeTestRepo = WelcomeTestRepo(httpClient, baseUrl)

        initialized = true
    }

    fun clear() {

        initialized = false
        _userRepo = null
        _unitRepo = null
        _exerciseRepo = null
        _questionRepo = null
        _testRepo = null
        _wordRepo = null
        _welcomeTestRepo = null
    }

    fun checkInitialized() {
        check(initialized) { "RepositoryProvider not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories." }
    }
}