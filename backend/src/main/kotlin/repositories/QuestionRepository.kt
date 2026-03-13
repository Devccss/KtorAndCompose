package repositories

import com.example.dtos.AlternativeDto
import com.example.dtos.CreateAlternativeDto
import com.example.dtos.CreateQuestionCompletedDto
import com.example.dtos.CreateQuestionDto
import com.example.dtos.QuestionCompletedDto
import com.example.dtos.QuestionDto
import com.example.dtos.UpdateAlternativeDto
import com.example.dtos.UpdateQuestionCompletedDto
import com.example.dtos.UpdateQuestionDto
import io.ktor.server.plugins.BadRequestException
import models.Alternatives
import models.Questions
import models.QuestionsCompleted
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
            exerciseContentId = row[Questions.exerciseContentId],
            questionText = row[Questions.questionText],
            orderQuestion = row[Questions.orderQuestion],
            isActive = row[Questions.isActive],
            createdAt = row[Questions.createdAt].toString()
        )
    }
    private fun resultRowToAlternative(row: ResultRow): AlternativeDto {
        return AlternativeDto(
            id = row[Alternatives.id].value,
            questionId = row[Alternatives.questionId],
            text = row[Alternatives.text],
            isCorrect = row[Alternatives.isCorrect],
            createdAt = row[Alternatives.createdAt].toString()
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
    fun getQuestionsByExerciseId(exerciseContentId: Int): List<QuestionDto> = transaction {
        Questions.selectAll().where { Questions.exerciseContentId eq exerciseContentId }.orderBy(Questions.createdAt).map(::resultRowToQuestion)
    }

    fun getQuestionById(id: Int): QuestionDto? = transaction {
        Questions.selectAll().where { Questions.id eq id }.singleOrNull()?.let(::resultRowToQuestion)
    }

    fun createQuestion(exerciseContent: Int,dto: CreateQuestionDto): QuestionDto = try {
        transaction {
            var order= 0
            if(dto.orderQuestion == null) {
                val total = (Questions.selectAll().count() + 1)
                order = total.toInt()
            }
            val newId = Questions.insert {
                it[exerciseContentId] = exerciseContent
                it[questionText] = dto.questionText
                it[orderQuestion] = dto.orderQuestion?: order
                it[isActive] = dto.isActive ?: false
                it[createdAt] = LocalDateTime.now()
            }[Questions.id]

            QuestionDto(
                id = newId.value,
                exerciseContentId = exerciseContent,
                isActive = dto.isActive ?: false,
                questionText = dto.questionText,
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
                dto.exerciseContentId?.let { update[exerciseContentId] = it }
                dto.questionText?.let { update[questionText] = it }
                dto.orderQuestion?.let { update[orderQuestion] = it}
                dto.isActive?.let { update[isActive] = it }
            }
            return@transaction true
        }
    }

    fun deleteQuestion(id: Int): Boolean = transaction {
        getQuestionById(id) ?: throw BadRequestException("La pregunta con ID $id no existe.")
        Questions.deleteWhere { Questions.id eq id } > 0
    }


    /* Alternatives */
    fun getAllAlternatives(): List<AlternativeDto> = transaction {
        Alternatives.selectAll().orderBy(Alternatives.createdAt).map(::resultRowToAlternative)
    }
    fun getAlternativesByQuestionId(questionId: Int): List<AlternativeDto> = transaction {
        Alternatives.selectAll().where { Alternatives.questionId eq questionId }.map(::resultRowToAlternative)
    }
    fun createAlternative(question: Int,dto: CreateAlternativeDto): AlternativeDto = try {
        transaction {
            val newId = Alternatives.insert {
                it[questionId] = question
                it[text] = dto.text
                it[isCorrect] = dto.isCorrect?: false
            }[Alternatives.id]

            AlternativeDto(
                id = newId.value,
                questionId = question,
                text = dto.text,
                isCorrect = dto.isCorrect?: false,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear la alternativa: ${e.message}")
    }
    fun updateAlternative(id: Int, dto: UpdateAlternativeDto) {
        transaction {
            val existing = Alternatives.selectAll().where { Alternatives.id eq id }.singleOrNull()
                ?: throw BadRequestException("La alternativa con ID $id no existe.")

            Alternatives.update({ Alternatives.id eq id }) { update ->
                if(dto.text?.isNotBlank() == true){
                    update[text] = dto.text
                }
                update[isCorrect] = dto.isCorrect?: existing[isCorrect]
            }
        }
    }
    fun deleteAlternative(id: Int): Boolean = transaction {
        val existing = Alternatives.selectAll().where { Alternatives.id eq id }.singleOrNull()
            ?: throw BadRequestException("La alternativa con ID $id no existe.")
        Alternatives.deleteWhere { Alternatives.id eq id } > 0
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
