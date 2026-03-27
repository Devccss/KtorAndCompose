package repositories
import com.example.dtos.CreateExerciseWordDto
import com.example.dtos.ExerciseWordDto
import com.example.dtos.UpdateExerciseWordDto
import io.ktor.server.plugins.BadRequestException
import models.ExerciseWords
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class ExerciseWordsRepository {

    private fun resultRowToContentWord(row: ResultRow): ExerciseWordDto {
        return ExerciseWordDto(
            id = row[ExerciseWords.id].value,
            exerciseId = row[ExerciseWords.exerciseId],
            wordId = row[ExerciseWords.wordId]
        )
    }

    fun getAll(): List<ExerciseWordDto> = transaction {
        ExerciseWords.selectAll().map(::resultRowToContentWord)
    }

    fun getById(id: Int): ExerciseWordDto? = transaction {
        ExerciseWords.selectAll().where { ExerciseWords.id eq id }.singleOrNull()?.let(::resultRowToContentWord)
    }

    fun getByExerciseId(exerciseId: Int): List<ExerciseWordDto> = transaction {
        ExerciseWords.selectAll().where { ExerciseWords.exerciseId eq exerciseId }.map(::resultRowToContentWord)
    }

    fun create(dto: CreateExerciseWordDto): ExerciseWordDto = try {

        transaction {
            val questionWord = ExerciseWords.selectAll().where{
                (ExerciseWords.exerciseId eq dto.exerciseId) and
                        (ExerciseWords.wordId eq dto.wordId)
            }
            if (!questionWord.empty()) {
                throw BadRequestException("La relación entre la pregunta y la palabra ya existe.")
            }
            val newId = ExerciseWords.insert {
                it[exerciseId] = dto.exerciseId
                it[wordId] = dto.wordId
            }[ExerciseWords.id]

            ExerciseWordDto(id = newId.value, exerciseId = dto.exerciseId, wordId = dto.wordId)
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear QuestionWord: ${e.message}")
    }

    fun update(id: Int, dto: UpdateExerciseWordDto): Boolean = transaction {
        getById(id) ?: throw BadRequestException("ExercieWord con ID $id no existe.")
        val updatedRows = ExerciseWords.update({ ExerciseWords.id eq id }) { u ->
            dto.exerciseId?.let { u[exerciseId] = it }
            dto.wordId?.let { u[wordId] = it }
        }
        updatedRows > 0
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("ExerciseWord con ID $id no existe.")
        ExerciseWords.deleteWhere { ExerciseWords.id eq id } > 0
    }
}
