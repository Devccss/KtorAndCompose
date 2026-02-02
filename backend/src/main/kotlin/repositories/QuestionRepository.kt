package repositories

import com.example.dtos.CreateQuestionCompletedDto
import com.example.dtos.CreateQuestionDto
import com.example.dtos.QuestionCompletedDto
import com.example.dtos.QuestionDto
import com.example.dtos.UpdateQuestionCompletedDto
import com.example.dtos.UpdateQuestionDto
import io.ktor.server.plugins.BadRequestException
import models.Questions
import models.QuestionsCompleted
import models.Units
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
            textContent = row[Questions.textContent],
            typeText = row[Questions.typeText],
            grammarExplanation = row[Questions.grammarExplanation],
            audioUrl = row[Questions.audioUrl],
            isActive = row[Questions.isActive],
            exerciseId = row[Questions.exerciseId],
            questionText = row[Questions.questionText],
            typeQuestion = row[Questions.typeQuestion],
            orderQuestion = row[Questions.orderQuestion],
            createdAt = row[Questions.createdAt].toString()
        )
    }

    private fun resultRowToQuestionCompleted(row: ResultRow): QuestionCompletedDto {
        return QuestionCompletedDto(
            id = row[QuestionsCompleted.id].value,
            userId = row[QuestionsCompleted.userId],
            questionId = row[QuestionsCompleted.questionId],
            completedAt = row[QuestionsCompleted.completionDate].toString()
        )
    }

    fun getAllQuestions(): List<QuestionDto> = transaction {
        Questions.selectAll().orderBy(Questions.createdAt).map(::resultRowToQuestion)
    }

    fun getQuestionById(id: Int): QuestionDto? = transaction {
        Questions.selectAll().where { Questions.id eq id }.singleOrNull()?.let(::resultRowToQuestion)
    }

    fun createQuestion(dto: CreateQuestionDto): QuestionDto = try {
        transaction {
            var order= 0
            if(dto.orderQuestion == null) {
                val total = (Questions.selectAll().count() + 1)
                order = total.toInt()
            }
            val newId = Questions.insert {
                it[textContent] = dto.textContent
                it[typeText] = dto.typeText
                it[grammarExplanation] = dto.grammarExplanation
                it[audioUrl] = dto.audioUrl
                it[isActive] = dto.isActive ?: false
                it[exerciseId] = dto.exerciseId
                it[questionText] = dto.questionText
                it[typeQuestion] = dto.typeQuestion
                it[orderQuestion] = dto.orderQuestion?: order
            }[Questions.id]

            QuestionDto(
                id = newId.value,
                textContent = dto.textContent,
                typeText = dto.typeText,
                grammarExplanation = dto.grammarExplanation,
                audioUrl = dto.audioUrl,
                isActive = dto.isActive ?: false,
                exerciseId = dto.exerciseId,
                questionText = dto.questionText,
                typeQuestion = dto.typeQuestion,
                orderQuestion = dto.orderQuestion ?: order,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear la pregunta: ${e.message}")
    }

    fun updateQuestion(id: Int, dto: UpdateQuestionDto) {
        transaction {
            getQuestionById(id) ?: throw BadRequestException("La pregunta con ID $id no existe.")

            Questions.update({ Questions.id eq id }) { update ->
                dto.textContent?.let { update[textContent] = it }
                dto.typeText?.let { update[typeText] = it }
                dto.grammarExplanation?.let { update[grammarExplanation] = it }
                dto.audioUrl?.let { update[audioUrl] = it }
                dto.isActive?.let { update[isActive] = it }
                dto.exerciseId?.let { update[exerciseId] = it }
                dto.questionText?.let { update[questionText] = it }
                dto.typeQuestion?.let { update[typeQuestion] = it }
                dto.orderQuestion?.let { update[orderQuestion] = it}
            }
        }
    }

    fun deleteQuestion(id: Int): Boolean = transaction {
        getQuestionById(id) ?: throw BadRequestException("La pregunta con ID $id no existe.")
        Questions.deleteWhere { Questions.id eq id } > 0
    }

    /* QuestionCompleted operations */

    fun createQuestionsCompleted(dto: CreateQuestionCompletedDto): QuestionCompletedDto = try {
        transaction {
            val newId = QuestionsCompleted.insert {
                it[userId] = dto.userId
                it[questionId] = dto.questionId
                it[completionDate] = LocalDateTime.now()
            }[QuestionsCompleted.id]

            QuestionCompletedDto(
                id = newId.value,
                userId = dto.userId,
                questionId = dto.questionId,
                completedAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear la pregunta completada: ${e.message}")
    }

    fun updateQuestionsCompleted(id: Int, dto: UpdateQuestionCompletedDto) {
        transaction {
            getQuestionsCompletedById(id) ?: throw BadRequestException("La pregunta completada con ID $id no existe.")

            QuestionsCompleted.update({ QuestionsCompleted.id eq id }) { update ->
                dto.userId?.let { update[userId] = it }
                dto.questionId?.let { update[questionId] = it }
                // actualizar marca de tiempo al editar
                update[completionDate] = LocalDateTime.now()
            }
        }
    }

    fun getQuestionsCompletedByUser(userId: Int): List<QuestionCompletedDto> = transaction {
        QuestionsCompleted.selectAll().where { QuestionsCompleted.userId eq userId }.map(::resultRowToQuestionCompleted)
    }

    fun getQuestionsCompletedById(id: Int): QuestionCompletedDto? = transaction {
        QuestionsCompleted.selectAll().where { QuestionsCompleted.id eq id }.singleOrNull()?.let(::resultRowToQuestionCompleted)
    }

    fun getAllQuestionsCompleted(): List<QuestionCompletedDto> = transaction {
        QuestionsCompleted.selectAll().map(::resultRowToQuestionCompleted)
    }

    fun deleteQuestionsCompleted(id: Int): Boolean = transaction {
        getQuestionsCompletedById(id) ?: throw BadRequestException("La pregunta completada con ID $id no existe.")
        QuestionsCompleted.deleteWhere { QuestionsCompleted.id eq id } > 0
    }
}
