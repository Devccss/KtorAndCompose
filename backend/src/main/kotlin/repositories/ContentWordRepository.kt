package repositories

import com.example.dtos.ContentWordDto
import com.example.dtos.CreateContentWordDto
import com.example.dtos.UpdateContentWordDto
import io.ktor.server.plugins.BadRequestException
import models.ContentWords

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class ContentWordRepository {

    private fun resultRowToContentWord(row: ResultRow): ContentWordDto {
        return ContentWordDto(
            id = row[ContentWords.id].value,
            contentId = row[ContentWords.contentId],
            wordId = row[ContentWords.wordId]
        )
    }

    fun getAll(): List<ContentWordDto> = transaction {
        ContentWords.selectAll().map(::resultRowToContentWord)
    }

    fun getById(id: Int): ContentWordDto? = transaction {
        ContentWords.selectAll().where { ContentWords.id eq id }.singleOrNull()?.let(::resultRowToContentWord)
    }

    fun create(dto: CreateContentWordDto): ContentWordDto = try {
        transaction {
            val newId = ContentWords.insert {
                it[contentId] = dto.contentId
                it[wordId] = dto.wordId
            }[ContentWords.id]

            ContentWordDto(id = newId.value, contentId = dto.contentId, wordId = dto.wordId)
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear contentWord: ${e.message}")
    }

    fun update(id: Int, dto: UpdateContentWordDto) {
        transaction {
            getById(id) ?: throw BadRequestException("ContentWord con ID $id no existe.")
            ContentWords.update({ ContentWords.id eq id }) { u ->
                dto.contentId?.let { u[ContentWords.contentId] = it }
                dto.wordId?.let { u[ContentWords.wordId] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("ContentWord con ID $id no existe.")
        ContentWords.deleteWhere { ContentWords.id eq id } > 0
    }
}
