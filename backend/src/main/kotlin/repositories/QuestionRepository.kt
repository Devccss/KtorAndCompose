package repositories

import com.example.dtos.CreateQuestionDto
import com.example.dtos.QuestionDto
import com.example.dtos.UpdateQuestionDto
import io.ktor.server.plugins.BadRequestException
import models.Questions
import models.TypeQuestion

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class QuestionRepository {

    private fun resultRowToQuestion(row: ResultRow): QuestionDto {
        return QuestionDto(
            id = row[Questions.id].value,
            questionText = row[Questions.questionText],
            typeQuestion = row[Questions.typeQuestion],
            contentId = row[Questions.contentId],
            createdAt = row[Questions.createdAt].toString()
        )
    }

    fun getAll(): List<QuestionDto> = transaction {
        Questions.selectAll().orderBy(Questions.createdAt).map(::resultRowToQuestion)
    }

    fun getById(id: Int): QuestionDto? = transaction {
        Questions.selectAll().where { Questions.id eq id }.singleOrNull()?.let(::resultRowToQuestion)
    }

    fun create(dto: CreateQuestionDto): QuestionDto = try {
        transaction {
            val newId = Questions.insert {
                it[questionText] = dto.questionText
                it[typeQuestion] = dto.typeQuestion ?: TypeQuestion.OPEN
                it[contentId] = dto.contentId
            }[Questions.id]

            QuestionDto(
                id = newId.value,
                questionText = dto.questionText,
                typeQuestion = dto.typeQuestion ?: TypeQuestion.OPEN,
                contentId = dto.contentId,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear pregunta: ${e.message}")
    }

    fun update(id: Int, dto: UpdateQuestionDto) {
        transaction {
            getById(id) ?: throw BadRequestException("Question con ID $id no existe.")
            Questions.update({ Questions.id eq id }) { u ->
                dto.questionText?.let { u[questionText] = it }
                dto.typeQuestion?.let { u[typeQuestion] = it }
                dto.contentId?.let { u[contentId] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("Question con ID $id no existe.")
        Questions.deleteWhere { Questions.id eq id } > 0
    }
}
