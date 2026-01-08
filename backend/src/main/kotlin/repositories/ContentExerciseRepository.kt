package repositories

import com.example.dtos.ContentExerciseDto
import com.example.dtos.CreateContentExerciseDto
import com.example.dtos.UpdateContentExerciseDto
import io.ktor.server.plugins.BadRequestException
import models.ContentExercises
import models.TypeTextExercise

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class ContentExerciseRepository {

    private fun resultRowToContent(row: ResultRow): ContentExerciseDto {
        return ContentExerciseDto(
            id = row[ContentExercises.id].value,
            nameExercise = row[ContentExercises.nameExercise],
            typeText = row[ContentExercises.typeText],
            audioUrl = row[ContentExercises.audioUrl],
            isActive = row[ContentExercises.isActive],
            createdAt = row[ContentExercises.createdAt].toString(),
            exerciseId = row[ContentExercises.exerciseId]
        )
    }

    fun getAll(): List<ContentExerciseDto> = transaction {
        ContentExercises.selectAll().orderBy(ContentExercises.createdAt).map(::resultRowToContent)
    }

    fun getById(id: Int): ContentExerciseDto? = transaction {
        ContentExercises.selectAll().where { ContentExercises.id eq id }.singleOrNull()?.let(::resultRowToContent)
    }

    fun create(dto: CreateContentExerciseDto): ContentExerciseDto = try {
        transaction {
            val newId = ContentExercises.insert {
                it[nameExercise] = dto.nameExercise
                it[typeText] = dto.typeText
                it[audioUrl] = dto.audioUrl
                it[isActive] = dto.isActive ?: false
                it[exerciseId] = dto.exerciseId
            }[ContentExercises.id]

            ContentExerciseDto(
                id = newId.value,
                nameExercise = dto.nameExercise,
                typeText = dto.typeText,
                audioUrl = dto.audioUrl,
                isActive = dto.isActive ?: false,
                createdAt = LocalDateTime.now().toString(),
                exerciseId = dto.exerciseId
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear contentExercise: ${e.message}")
    }

    fun update(id: Int, dto: UpdateContentExerciseDto) {
        transaction {
            getById(id) ?: throw BadRequestException("ContentExercise con ID $id no existe.")
            ContentExercises.update({ ContentExercises.id eq id }) { u ->
                dto.nameExercise?.let { u[ContentExercises.nameExercise] = it }
                dto.typeText?.let { u[ContentExercises.typeText] = it }
                dto.audioUrl?.let { u[ContentExercises.audioUrl] = it }
                dto.isActive?.let { u[ContentExercises.isActive] = it }
                dto.exerciseId?.let { u[ContentExercises.exerciseId] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("ContentExercise con ID $id no existe.")
        ContentExercises.deleteWhere { ContentExercises.id eq id } > 0
    }
}
