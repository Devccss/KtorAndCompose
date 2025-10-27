package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateTest
import org.example.project.models.Level
import org.example.project.models.Test
import org.example.project.repository.KtorLevelRepository
import org.example.project.repository.TestRepository


data class TestUiState(
    val tests: List<Test> = emptyList(),
    val levels: List<Level> = emptyList(),
    val isLoading: Boolean = false,
    var error: String? = null,
    val message: String? = null
)

class TestViewModel(private val repo: TestRepository) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(TestUiState(isLoading = true))
    val state: StateFlow<TestUiState> = _state


    init {
        refresh()
        getLevels()
    }


    fun createTest(newTest: CreateTest, levelId: Int) {
        launchCatching(
            block = { repo.createTest(newTest, levelId) },
            onSuccess = {
                _state.value = _state.value.copy(
                    tests = _state.value.tests + it
                )
                _state.value = _state.value.copy(
                    message = "Test creado exitosamente"
                )
            }

        )
    }

    fun addDialogTest(dialogId: Int, testId: Int) {
        launchCatching(
            block = { repo.addDialogTest(dialogId, testId) },
            onSuccess = {
                _state.value = _state.value.copy(
                    message = "Diálogo agregado al test exitosamente"
                )
                refresh()
            }
        )
    }

    fun editTest(editedTest: CreateTest, testId: Int) {
        launchCatching(
            block = { repo.editTest(editedTest, testId) },
            onSuccess = { updated ->

                if (updated){
                    _state.value = _state.value.copy(
                        message = "Test actualizado exitosamente"
                    )
                    refresh()
                }else{
                    _state.value = _state.value.copy(
                        error = "Error al actualizar el test"
                    )
                }
            }
        )
    }

    fun deleteTest(testId: Int) {
        launchCatching(
            block = { repo.deleteTest(testId) },
            onSuccess = { result ->
                if (result) {
                    _state.value = _state.value.copy(
                        tests = _state.value.tests.filterNot { it.id == testId }
                    )
                } else if(!result) {
                    _state.value = _state.value.copy(error = "El test tiene dialogos asociados")
                }else{
                    _state.value = _state.value.copy(error = "Error al eliminar el test")
                }
            }
        )
    }


    private fun getLevels() {
        launchCatching(
            block = {
                repo.getAllLevelsFromTestRepo()
            },
            onSuccess = { levels -> _state.value = _state.value.copy(levels = levels) }
        )
    }

     fun refresh() {
        launchCatching(
            block = { repo.getAllTests() },
            onSuccess = { tests -> _state.value = _state.value.copy(tests = tests) }
        )
    }

    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            onSuccess(block())
            _state.value = _state.value.copy(isLoading = false)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

}