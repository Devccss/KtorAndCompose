package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.ProgressDto
import org.example.project.dtos.UsersDto
import org.example.project.repository.StudentRepository

data class StudentUiState(
    val studentDetails: UsersDto? = null,
    val studentProgress: ProgressDto? = null,
    val studentDialogs: Any? = null,
    val fullStudentDialog: Any? = null,
    val standbyPhrases: List<Any>? = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class StudentViewModel(val repo: StudentRepository,val student:UsersDto): ViewModel(),ScreenModel {
    private val _state = MutableStateFlow(
        StudentUiState(
            studentDetails = student,
            isLoading = true,
        )
    )
    val state : StateFlow<StudentUiState> = _state

    init {
        loadStudentProgress(student.id)
    }

    private fun loadStudentProgress(studentId: Int) {
        launchCatching(
            block = { repo.getStudentProgress(studentId) },
            onSuccess = { progress ->
                _state.value = StudentUiState(

                    studentProgress = progress as ProgressDto,
                    isLoading = false
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,
                    isLoading = false
                )
            }
        )
    }

    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            onSuccess(block())
            _state.value = _state.value.copy(isLoading = false)

        } catch (e: Exception) {
            onError(e)
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }
}