package com.example.services

import com.example.dtos.CreateParticipantDTO
import com.example.dtos.DialogParticipantDTO
import com.example.dtos.UpdateParticipantDTO
import com.example.repositories.DialogParticipantsRepository

class DialogParticipantsService(private val dialogParticipantsRepository: DialogParticipantsRepository) {

    fun createDialogParticipant(dialogId: Int, participant: CreateParticipantDTO): DialogParticipantDTO {
        return dialogParticipantsRepository.createDialogParticipants(participant, dialogId)
    }
    fun getParticipantById(participantId: Int): DialogParticipantDTO? {
        return dialogParticipantsRepository.getParticipantById(participantId)
    }

    fun getParticipantsByDialogId(dialogId: Int): List<DialogParticipantDTO> {
        return dialogParticipantsRepository.getDialogParticipantsByDialogId(dialogId)
    }
    fun deleteDialogParticipant(participantId: Int): Boolean {
        return dialogParticipantsRepository.deleteDialogParticipant(participantId)
    }
    fun updateDialogParticipant(participantId: Int, participant: UpdateParticipantDTO): Int {
        return dialogParticipantsRepository.updateDialogParticipant(participantId, participant)
    }
}