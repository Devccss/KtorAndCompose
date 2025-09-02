package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateParticipantDTO
import org.example.project.dtos.CreatePhraseDto
import org.example.project.dtos.DialogDetailDTO
import org.example.project.dtos.DialogParticipantDTO
import org.example.project.dtos.OrderPhraseDto
import org.example.project.dtos.PhraseDto
import org.example.project.dtos.WordDto
import org.example.project.models.Dialog
import org.example.project.models.Level
import org.example.project.repository.ParticipantsRepository
import org.example.project.repository.PhraseRepository
import org.example.project.repository.PhraseWordRepository
import org.example.project.repository.WordRepository
import org.example.project.repository.dialogsRepository.DialogsRepository

data class DialogDetailsUIState(
    val fullDialog: DialogDetailDTO? = null,
    val level: Level? = null,
    val levels: List<Level> = emptyList(),
    val participants: List<DialogParticipantDTO> = emptyList(),
    val phrases: List<PhraseDto> = emptyList(),
    val words: List<WordDto> = emptyList(),
    val phraseOrder: List<OrderPhraseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DialogDetailsViewModel(
    private val dialogId: Int,
    private val dialogsRepository: DialogsRepository,
    private val repoParticipant: ParticipantsRepository,
    private val repoPhrase: PhraseRepository,
    private val repoWord: WordRepository,
    private val repoPhraseWords: PhraseWordRepository
) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(DialogDetailsUIState(isLoading = true))
    val state: StateFlow<DialogDetailsUIState> = _state

    init {
        fun loadDialogDetails() {
            launchCatching(
                block = { dialogsRepository.getFullDialogById(dialogId) },
                onSuccess = { fullDialog ->
                    getDialogLevel(fullDialog.dialog.levelId)
                    _state.value = _state.value.copy(
                        fullDialog = fullDialog,
                        phrases = fullDialog.participants.flatMap { it.phrases }.map { it.phrase },
                        participants = fullDialog.participants.map { it.participant },
                        words = fullDialog.participants.flatMap { it.phrases }.flatMap { it.words },
                        phraseOrder = fullDialog.participants.flatMap { it.phrases }
                            .mapNotNull { phrases ->
                                phrases.order?.let {
                                    OrderPhraseDto(
                                        dialogId,
                                        phrases.phrase.id,
                                        it
                                    )
                                }
                            }
                    )

                }
            )
        }
        getLevels()
        loadDialogDetails()
    }


    private fun getDialogLevel(id: Int) {
        launchCatching(
            block = { dialogsRepository.getDialogLevelByLevelId(id) },
            onSuccess = { level ->
                _state.value = _state.value.copy(
                    level = level
                )
            }
        )
    }

    private fun getFullDialogById(id: Int) {
        launchCatching(
            block = { dialogsRepository.getFullDialogById(id) },
            onSuccess = { fullDialog ->

                _state.value = _state.value.copy(
                    fullDialog = fullDialog
                )
            }
        )
    }

    fun updateDialog(id: Int, dialog: Dialog) {
        launchCatching(
            block = { dialogsRepository.updateDialog(id, dialog) },
            onSuccess = { getFullDialogById(id) }
        )
    }

    fun deleteDialog(id: Int) {
        launchCatching(
            block = { dialogsRepository.deleteDialog(id) },
            onSuccess = {
                _state.value = _state.value.copy(
                    fullDialog = null,
                    phrases = emptyList(),
                    words = emptyList(),
                    phraseOrder = emptyList()
                )
            }
        )
    }

    private fun getLevels() {
        launchCatching(
            block = { dialogsRepository.getAllLevelsFromDialogsRepo() },
            onSuccess = { levels ->
                _state.value = _state.value.copy(
                    levels = levels

                )
            }
        )
    }

    fun createParticipant(dialogId: Int, participant: CreateParticipantDTO) {
        launchCatching(
            block = {
                repoParticipant.createParticipant(dialogId, participant)
            },
            onSuccess = { newParticipant ->
                _state.value =
                    _state.value.copy(participants = _state.value.participants + newParticipant)

            }
        )
    }

    private fun getParticipantById(participantId: Int) {
        launchCatching(
            block = { repoParticipant.getParticipantById(participantId) },
            onSuccess = { participant ->
                _state.update { currentState ->

                    val updatedParticipants = currentState.participants.map { p ->
                        if (p.id == participantId) participant else p
                    }

                    currentState.copy( participants = updatedParticipants )
                }
            }
        )
    }
    private fun getParticipantsByDialogId(dialogId: Int) {
        launchCatching(
            block = { repoParticipant.getParticipantsByDialogId(dialogId) },
            onSuccess = { participants ->
                _state.value = _state.value.copy(participants = participants)
            }
        )
    }

    fun updateParticipant(participantId: Int, participant: CreateParticipantDTO) {
        launchCatching(
            block = {
                repoParticipant.updateParticipant(participantId, participant)
            },
            onSuccess = {getParticipantById(participantId)}
        )
    }

    fun deleteParticipant(participantId: Int) {
        launchCatching(
            block = { repoParticipant.deleteParticipant(participantId) },
            onSuccess = { getParticipantsByDialogId(dialogId) }
        )
    }


    fun createPhrase(participantId: Int, phrase: CreatePhraseDto) {
        launchCatching(
            block = { repoPhrase.createPhrase(participantId, phrase) },
            onSuccess = { newPhrase ->
                val updatedPhrases = _state.value.phrases + newPhrase
                _state.value = _state.value.copy(phrases = updatedPhrases)
            }
        )
    }

    fun updatePhrase(phraseId: Int, phrase: CreatePhraseDto) {
        launchCatching(
            block = { repoPhrase.updatePhrase(phraseId, phrase) },
            onSuccess = { getFullDialogById(dialogId) }
        )
    }

    fun deletePhrase(phraseId: Int) {
        launchCatching(
            block = { repoPhrase.deletePhrase(phraseId) },
            onSuccess = { getFullDialogById(dialogId) }
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