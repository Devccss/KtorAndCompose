package repositories
import com.example.dtos.CreateQuestionWordDto
import com.example.dtos.QuestionWordDto
import com.example.dtos.UpdateQuestionWordDto
import io.ktor.server.plugins.BadRequestException
import models.QuestionWords
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class QuestionWordsRepository {

    private fun resultRowToContentWord(row: ResultRow): QuestionWordDto {
        return QuestionWordDto(
            id = row[QuestionWords.id].value,
            questionId = row[QuestionWords.questionId],
            wordId = row[QuestionWords.wordId]
        )
    }

    fun getAll(): List<QuestionWordDto> = transaction {
        QuestionWords.selectAll().map(::resultRowToContentWord)
    }

    fun getById(id: Int): QuestionWordDto? = transaction {
        QuestionWords.selectAll().where { QuestionWords.id eq id }.singleOrNull()?.let(::resultRowToContentWord)
    }

    fun create(dto: CreateQuestionWordDto): QuestionWordDto = try {

        transaction {
            val questionWord = QuestionWords.selectAll().where{
                (QuestionWords.questionId eq dto.questionId) and
                        (QuestionWords.wordId eq dto.wordId)
            }
            if (!questionWord.empty()) {
                throw BadRequestException("La relación entre la pregunta y la palabra ya existe.")
            }
            val newId = QuestionWords.insert {
                it[questionId] = dto.questionId
                it[wordId] = dto.wordId
            }[QuestionWords.id]

            QuestionWordDto(id = newId.value, questionId = dto.questionId, wordId = dto.wordId)
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear QuestionWord: ${e.message}")
    }

    fun update(id: Int, dto: UpdateQuestionWordDto) {
        transaction {
            getById(id) ?: throw BadRequestException("QuestionWord con ID $id no existe.")
            QuestionWords.update({ QuestionWords.id eq id }) { u ->
                dto.questionId?.let { u[QuestionWords.questionId] = it }
                dto.wordId?.let { u[QuestionWords.wordId] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("ContentWord con ID $id no existe.")
        QuestionWords.deleteWhere { QuestionWords.id eq id } > 0
    }
}
