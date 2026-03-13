package com.example.repositories

import com.example.dtos.CreateExerciseContentDto
import com.example.dtos.ExerciseContentDto
import com.example.dtos.UpdateExerciseContentDto
import models.ExerciseContent
import org.h2.result.Row
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class ExerciseContentRepository {
    fun ExerciseContentRow(row: ResultRow): ExerciseContentDto {
        return ExerciseContentDto(
            id = row[ExerciseContent.id].value,
            exerciseId = row[ExerciseContent.exerciseId],
            contentType = row[ExerciseContent.contentType],
            textContent = row[ExerciseContent.textContent],
            grammarExplanation = row[ExerciseContent.grammarExplanation],
            audioUrl = row[ExerciseContent.audioUrl],
            createdAt = row[ExerciseContent.createdAt].toString()
        )
    }

    fun createExerciseContent(exercise: Int, exerciseContent: CreateExerciseContentDto): ExerciseContentDto =
        transaction {
            val newContentId = ExerciseContent.insert {
                it[exerciseId] = exercise
                it[contentType] = exerciseContent.contentType
                it[textContent] = exerciseContent.textContent
                it[grammarExplanation] = exerciseContent.grammarExplanation
                it[audioUrl] = exerciseContent.audioUrl

            }[ExerciseContent.id]

            ExerciseContentDto(
                id = newContentId.value,
                exerciseId = exercise,
                contentType = exerciseContent.contentType,
                textContent = exerciseContent.textContent,
                grammarExplanation = exerciseContent.grammarExplanation,
                audioUrl = exerciseContent.audioUrl,
                createdAt = java.time.LocalDateTime.now().toString()
            )
        }

    fun getByExerciseId(exerciseId: Int): ExerciseContentDto? = transaction {
        ExerciseContent.selectAll().where { ExerciseContent.exerciseId eq exerciseId }
            .singleOrNull()
            ?.let(::ExerciseContentRow)
    }

    fun getAllExerciseContent(): List<ExerciseContentDto> = transaction {
        ExerciseContent.selectAll().map(::ExerciseContentRow)
    }

    fun updateContentByExerciseId(exercise: Int, updatedContent: UpdateExerciseContentDto): Boolean = transaction {
        ExerciseContent.update({ ExerciseContent.exerciseId eq exercise }) {up->
            updatedContent.contentType?.let { up[contentType] = it }
            updatedContent.textContent?.let { up[textContent] = it }
            updatedContent.grammarExplanation?.let { up[grammarExplanation] = it }
            updatedContent.audioUrl?.let { up[audioUrl] = it }
        }
        return@transaction true
    }

    fun deleteByExerciseId(exerciseId: Int) = transaction {
        ExerciseContent.deleteWhere { ExerciseContent.exerciseId eq  exerciseId }
    }

}