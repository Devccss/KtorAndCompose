package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateTest
import org.example.project.models.Dialog
import org.example.project.models.Level
import org.example.project.models.Test
import org.example.project.repository.TestRepository


data class TestDetailsUIState (
    val test: Test? = null,
    val level: Level? = null,
    val levels: List<Level> = emptyList(),
    val dialogs: List<Dialog> = emptyList(),
    val assignDialogs: List<Dialog> = emptyList(),
    val isLoading: Boolean = false,
    var error: String? = null,
    val message: String? = null
)


class TestDetailsViewModel(private val testId:Int, private val testRepository: TestRepository ):ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(TestDetailsUIState(isLoading = true))
    val state: StateFlow<TestDetailsUIState> = _state

    init {
        getTestById(testId)
        getAllDialogs()
        getDialogsAssignedToTest(testId)

    }

    fun editTest(test: CreateTest) {
        launchCatching(
            block = { testRepository.editTest(test,testId) },
            onSuccess = {  success->
                if (success) {
                    _state.value = _state.value.copy(
                        message = "Test editado correctamente"
                    )
                    getTestById(testId)
                } else {
                    _state.value = _state.value.copy(
                        error = "Error al editar el test"
                    )
                }
            }
        )
    }

    fun deleteTestDetails(testId: Int) {
        launchCatching(
            block = { testRepository.deleteTest(testId) },
            onSuccess = { result ->
                if (result) {
                    _state.value = _state.value.copy(
                        test = null
                    )
                } else if(!result) {
                    _state.value = _state.value.copy(error = "El test tiene dialogos asociados")
                }else{
                    _state.value = _state.value.copy(error = "Error al eliminar el test")
                }
            }
        )
    }

    fun assignDialogToTest(dialogId: Int) {
        launchCatching(
            block = { testRepository.addDialogTest(dialogId, testId) },
            onSuccess = { success->
                if (success) {
                    _state.value = _state.value.copy(
                        message = "Diálogo asignado correctamente"
                    )
                    getTestById(testId)
                } else {
                    _state.value = _state.value.copy(
                        error = "Error al asignar el diálogo"
                    )
                }
                getDialogsAssignedToTest(testId)
            }
        )
    }

    private fun getTestById(testId: Int) {
        launchCatching(
            block = { testRepository.getTestById(testId) },
            onSuccess = { test ->
                if (test != null) {
                    getLevelById(test.levelId)
                }
                _state.value = _state.value.copy(
                    test = test
                )
            }
        )
    }

    private fun getAllDialogs() {
        launchCatching(
            block = { testRepository.getAllDialogsFromTestRepo() },
            onSuccess = { dialogs ->
                _state.value = _state.value.copy(
                    dialogs = dialogs
                )
            }
        )
    }
    private fun getDialogsAssignedToTest(testId: Int) {
        launchCatching(
            block = { testRepository.getAllTestDialogs(testId) },
            onSuccess = { dg ->
                _state.value = _state.value.copy(
                    assignDialogs = dg
                )
            }
        )
    }

    private fun getLevelById(levelId: Int) {
        launchCatching(
            block = { testRepository.getLevelByIdFromTestRepo(levelId) },
            onSuccess = { level ->
                _state.value = _state.value.copy(
                    level = level
                )
            }
        )
    }

    fun deleteDialogFromTest(dialogId: Int,testId: Int) {
        launchCatching(
            block = { testRepository.deleteDialogTest(dialogId,testId) },
            onSuccess = {
                _state.value = _state.value.copy(
                    message = "Diálogo eliminado del test correctamente"
                )
                getDialogsAssignedToTest(testId)
            }
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