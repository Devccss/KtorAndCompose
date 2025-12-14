package repositories;

import LevelDTO
import models.Exercises
import com.example.dtos.CreateDialogDTO
import com.example.dtos.DialogDTOs;
import com.example.dtos.UpdateDialogDTO
import io.ktor.server.plugins.BadRequestException
import models.DialogsTests
import models.Units

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
            id = row[Exercises.id].value,
            levelId = row[Exercises.levelId],
            isActive = row[Exercises.isActive],
            name = row[Exercises.name],
            description = row[Exercises.description],
            createdAt = row[Exercises.createdAt].toString()
        )

    }
    fun getAllDialogs(): List<DialogDTOs> = transaction {
        Exercises.selectAll().orderBy(Exercises.createdAt).map(::resultRowToDialog)
    }
    fun getDialogById(id: Int): DialogDTOs? = transaction {
        Exercises.selectAll().where { Exercises.id eq id }.singleOrNull()?.let(::resultRowToDialog)
    }
    fun getDialogLevelByLevelId(levelId: Int): LevelDTO? = transaction {
        Units.selectAll().where{ Units.id eq levelId }.singleOrNull()?.let (::resultRowToLevel)
    }
    fun getDialogsByLevelId(levelId: Int): List<DialogDTOs> = transaction {
        Exercises.selectAll().where { Exercises.levelId eq levelId }.map(::resultRowToDialog)
    }

    fun getAllTestDialogs(testId: Int): List<DialogDTOs> = transaction {
        DialogsTests.selectAll()
            .where { DialogsTests.testId eq testId }
            .map { it[DialogsTests.dialogId] }
            .distinct()
            .mapNotNull { dialogId ->
                Exercises.selectAll().where { Exercises.id eq dialogId }
                    .singleOrNull()
                    ?.let(::resultRowToDialog)
            }
    }

    fun createDialog(dto: CreateDialogDTO,idLevel:Int ): DialogDTOs = try {
        transaction {

            val dialogNew = Exercises.insert {
                it[levelId] = idLevel
                it[name] = dto.name
                it[description] = dto.description
                it[isActive] = dto.isActive ?: false
            }[Exercises.id]

            DialogDTOs(
                id = dialogNew.value,
                levelId = idLevel,
                name = dto.name,
                description = dto.description,
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

            Exercises.update({Exercises.id eq id}) { update->
                dto.name?.let { update[name] = dto.name }
                dto.levelId?.let { update[levelId] = dto.levelId }
                dto.description?.let { update[description] = dto.description }
                dto.isActive?.let { update[isActive] = dto.isActive }
            }

        }

    }
    fun deleteDialog(id: Int): Boolean = transaction{
        getDialogById(id) ?: throw BadRequestException("El diálogo con ID $id no existe.")


        Exercises.deleteWhere { Exercises.id eq id } > 0
    }




}
