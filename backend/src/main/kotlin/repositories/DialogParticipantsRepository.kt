package com.example.repositories

import models.DialogParticipants
import com.example.dtos.CreateParticipantDTO
import com.example.dtos.DialogParticipantDTO
import com.example.dtos.UpdateParticipantDTO
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class DialogParticipantsRepository {
    fun createDialogParticipants(dto: CreateParticipantDTO, dialogID: Int): DialogParticipantDTO =try{
        transaction {
            val newParticipant = DialogParticipants.insert {
                it[dialogId] = dialogID
                it[name] = dto.name
            }[DialogParticipants.id]

            DialogParticipantDTO(
                id = newParticipant.value,
                dialogId = dialogID,
                name = dto.name,
                createdAt = LocalDateTime.now().toString()
            )
        }
    }catch ( e: Exception) {
        throw BadRequestException("Error al crear el participante del diálogo: ${e.message}")
    }
    fun getParticipantById(participantId: Int): DialogParticipantDTO? = transaction {
        DialogParticipants.selectAll().where { DialogParticipants.id eq participantId }
            .singleOrNull()?.let {
                DialogParticipantDTO(
                    id = it[DialogParticipants.id].value ,
                    dialogId = it[DialogParticipants.dialogId] ,
                    name = it[DialogParticipants.name] ,
                    createdAt = it[DialogParticipants.createdAt].toString()
                )
            }
    }

    fun getDialogParticipantsByDialogId(dialogId: Int): List<DialogParticipantDTO> = transaction {
        DialogParticipants.selectAll().where { DialogParticipants.dialogId eq dialogId }
            .map { row ->
                DialogParticipantDTO(
                    id = row[DialogParticipants.id].value,
                    dialogId = row[DialogParticipants.dialogId],
                    name = row[DialogParticipants.name],
                    createdAt = row[DialogParticipants.createdAt].toString()
                )
            }
    }
    fun deleteDialogParticipant(participantId: Int): Boolean = transaction {
        DialogParticipants.deleteWhere { DialogParticipants.id eq participantId } > 0
    }
    fun updateDialogParticipant(participantId: Int, dto: UpdateParticipantDTO) = transaction {
        DialogParticipants.selectAll().where { DialogParticipants.id eq participantId }.singleOrNull()
            ?: throw BadRequestException("El participante con ID $participantId no existe.")

        DialogParticipants.update({DialogParticipants.id eq participantId}) { update ->
            update[name] = dto.name

        }
    }
}