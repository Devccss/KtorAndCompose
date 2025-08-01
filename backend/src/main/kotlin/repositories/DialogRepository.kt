package repositories;

import models.Dialogs
import com.example.dtos.CreateDialogDTO
import com.example.dtos.DialogDTOs;
import com.example.dtos.UpdateDialogDTO
import io.ktor.server.plugins.BadRequestException

import org.jetbrains.exposed.v1.core.ResultRow;
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class DialogRepository() {



    fun resultRowToDialog(row:ResultRow): DialogDTOs {
        return DialogDTOs(
            id = row[Dialogs.id].value,
            levelId = row[Dialogs.levelId],
            difficulty = row[Dialogs.difficulty],
            audioUrl = row[Dialogs.audioUrl],
            isActive = row[Dialogs.isActive],
            name = row[Dialogs.name],
            description = row[Dialogs.description],
            createdAt = row[Dialogs.createdAt].toString()
        )

    }
    fun getAllDialogs(): List<DialogDTOs> = transaction {
        Dialogs.selectAll().orderBy(Dialogs.createdAt).map(::resultRowToDialog)
    }
    fun getDialogById(id: Int): DialogDTOs? = transaction {
        Dialogs.selectAll().where { Dialogs.id eq id }.singleOrNull()?.let(::resultRowToDialog)
    }
    fun getDialogsByLevelId(levelId: Int): List<DialogDTOs> = transaction {
        Dialogs.selectAll().where { Dialogs.levelId eq levelId }.map(::resultRowToDialog)
    }
    fun createDialog(dto: CreateDialogDTO,idLevel:Int ): DialogDTOs = try {
        transaction {

            val dialogNew = Dialogs.insert {
                it[levelId] = idLevel
                it[name] = dto.name
                it[difficulty] = dto.difficulty
                it[description] = dto.description
                it[audioUrl] = dto.audioUrl
            }[Dialogs.id]

            DialogDTOs(
                id = dialogNew.value,
                levelId = idLevel,
                name = dto.name,
                difficulty = dto.difficulty,
                description = dto.description,
                audioUrl = dto.audioUrl,
                isActive = true,
                createdAt = LocalDateTime.now().toString()
            )

        }
    }catch (e: Exception) {
        throw BadRequestException("Error al crear el diálogo: ${e.message}")
    }


    fun updateDialog(id: Int, dto: UpdateDialogDTO) {

        transaction {
            getDialogById(id)
                ?: throw BadRequestException("El diálogo con ID $id no existe.")

            Dialogs.update({Dialogs.id eq id}) {update->
                dto.name?.let { update[name] = dto.name }
                dto.levelId?.let { update[levelId] = dto.levelId }
                dto.difficulty?.let { update[difficulty] = dto.difficulty }
                dto.description?.let { update[description] = dto.description }
                dto.audioUrl?.let { update[audioUrl] = dto.audioUrl }
                dto.isActive?.let { update[isActive] = dto.isActive }
            }
        }

    }
    fun deleteDialog(id: Int): Boolean = transaction{
        getDialogById(id) ?: throw BadRequestException("El diálogo con ID $id no existe.")

        Dialogs.deleteWhere { Dialogs.id eq id } > 0
    }

    //Participants


}
