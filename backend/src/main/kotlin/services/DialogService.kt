package com.example.services

import LevelDTO
import com.example.dtos.CreateDialogDTO
import com.example.dtos.DialogDTOs
import com.example.dtos.UpdateDialogDTO
import repositories.DialogRepository
import services.NotFoundException
import services.ValidationException

class DialogService(private val dialogsRepository: DialogRepository) {
    fun getAllDialogs(): List<DialogDTOs> {
        return dialogsRepository.getAllDialogs()
    }

    fun getDialogById(id: Int): DialogDTOs {
        return dialogsRepository.getDialogById(id) ?: throw NotFoundException("Dialog not found")
    }
    fun getDialogLevel(levelId: Int): LevelDTO {
        return dialogsRepository.getDialogLevelByLevelId(levelId) ?: throw NotFoundException("Level not found")
    }

    fun getDialogsByLevelId(levelId: Int): List<DialogDTOs> {
        return dialogsRepository.getDialogsByLevelId(levelId)
    }

    fun getTestsDialogs(testId: Int): List<DialogDTOs> {
        return dialogsRepository.getAllTestDialogs(testId)
    }

    fun createDialog(dialog: CreateDialogDTO, idLevel:Int): DialogDTOs {
        validateDialogCreation(dialog)

        return dialogsRepository.createDialog(dialog, idLevel)
    }

    fun updateDialog(id: Int, dialog: UpdateDialogDTO): DialogDTOs {
        dialogsRepository.updateDialog(id, dialog)
        return dialogsRepository.getDialogById(id)!!
    }

    fun deleteDialog(id: Int): Boolean {
        return dialogsRepository.deleteDialog(id)
    }

    private fun validateDialogCreation(dialog: CreateDialogDTO) {
        if (dialog.name.isBlank()) {
            throw ValidationException("Dialog name cannot be empty")
        }
        if (dialog.description?.isBlank() == true) {
            throw ValidationException("Dialog description cannot be empty")
        }
    }



}