package repositories

import com.example.dtos.CreateWordDto
import com.example.dtos.UpdateWordDto
import com.example.dtos.WordDto
import io.ktor.server.plugins.BadRequestException
import models.Words

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class WordRepository {

    private fun resultRowToWord(row: ResultRow): WordDto {
        return WordDto(
            id = row[Words.id].value,
            english = row[Words.english],
            spanish = row[Words.spanish],
            phonetic = row[Words.phonetic],
            description = row[Words.description],
            isActive = row[Words.isActive],
            createdAt = row[Words.createdAt].toString()
        )
    }

    fun getAll(): List<WordDto> = transaction {
        Words.selectAll().orderBy(Words.createdAt).map(::resultRowToWord)
    }

    fun getById(id: Int): WordDto? = transaction {
        Words.selectAll().where { Words.id eq id }.singleOrNull()?.let(::resultRowToWord)
    }

    fun create(dto: CreateWordDto): WordDto = try {
        transaction {
            val newId = Words.insert {
                it[english] = dto.english
                it[spanish] = dto.spanish
                it[phonetic] = dto.phonetic
                it[description] = dto.description
                it[isActive] = dto.isActive ?: false
            }[Words.id]

            WordDto(
                id = newId.value,
                english = dto.english,
                spanish = dto.spanish,
                phonetic = dto.phonetic,
                description = dto.description,
                isActive = dto.isActive ?: false,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear palabra: ${e.message}")
    }

    fun update(id: Int, dto: UpdateWordDto) {
        transaction {
            getById(id) ?: throw BadRequestException("Word con ID $id no existe.")
            Words.update({ Words.id eq id }) { u ->
                dto.english?.let { u[english] = it }
                dto.spanish?.let { u[spanish] = it }
                dto.phonetic?.let { u[phonetic] = it }
                dto.description?.let { u[description] = it }
                dto.isActive?.let { u[isActive] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("Word con ID $id no existe.")
        Words.deleteWhere { Words.id eq id } > 0
    }
}
