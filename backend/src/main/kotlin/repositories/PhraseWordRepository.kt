package com.example.repositories

import models.ExerciseWords
import com.example.dtos.CreatePhraseWordDto
import com.example.dtos.PhraseWordDto
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class PhraseWordRepository {
    fun createPhraseWord(dto: CreatePhraseWordDto): PhraseWordDto = try {
        transaction {
            val phraseWord = ExerciseWords.insert {
                it[phraseId] = dto.phraseId
                it[wordId] = dto.wordId
                it[order] = dto.order
            }[ExerciseWords.id]

            PhraseWordDto(
                id = phraseWord.value,
                phraseId = dto.phraseId,
                wordId = dto.wordId,
                order = dto.order
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error creating PhraseWord: ${e.message}")
    }

    fun getAllPhraseWords(): List<PhraseWordDto> = try {
        transaction {
            ExerciseWords.selectAll().map { row ->
                PhraseWordDto(
                    id = row[ExerciseWords.id].value,
                    phraseId = row[ExerciseWords.phraseId],
                    wordId = row[ExerciseWords.wordId],
                    select = row[ExerciseWords.selectToTranslate],
                    order = row[ExerciseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWords: ${e.message}")
    }

    fun getPhraseWordById(id: Int): PhraseWordDto? = try {
        transaction {
            ExerciseWords.selectAll().where { ExerciseWords.id eq id }.singleOrNull()?.let { row ->
                PhraseWordDto(
                    id = row[ExerciseWords.id].value,
                    phraseId = row[ExerciseWords.phraseId],
                    wordId = row[ExerciseWords.wordId],
                    order = row[ExerciseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWord by ID: ${e.message}")
    }

    fun getPhraseWordsByPhraseId(phraseId: Int): List<PhraseWordDto> = try {
        transaction {
            ExerciseWords.selectAll().where { ExerciseWords.phraseId eq phraseId }.map { row ->
                PhraseWordDto(
                    id = row[ExerciseWords.id].value,
                    phraseId = row[ExerciseWords.phraseId],
                    wordId = row[ExerciseWords.wordId],
                    order = row[ExerciseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWords by Phrase ID: ${e.message}")
    }


    fun getPhraseWordsByWordId(wordId: Int): List<PhraseWordDto> = try {
        transaction {
            ExerciseWords.selectAll().where { ExerciseWords.wordId eq wordId }.map { row ->
                PhraseWordDto(
                    id = row[ExerciseWords.id].value,
                    phraseId = row[ExerciseWords.phraseId],
                    wordId = row[ExerciseWords.wordId],
                    order = row[ExerciseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWords by Word ID: ${e.message}")
    }

    fun updatePhraseWord(id: Int, dto: CreatePhraseWordDto): PhraseWordDto? = try {
        transaction {
            ExerciseWords.update({ ExerciseWords.id eq id }) {
                it[phraseId] = dto.phraseId
                it[wordId] = dto.wordId
                it[order] = dto.order
            }
            getPhraseWordById(id)
        }
    } catch (e: Exception) {
        throw BadRequestException("Error updating PhraseWord: ${e.message}")
    }


    fun deletePhraseWord(id: Int): Boolean = try {
        transaction {
            val deletedRows = ExerciseWords.deleteWhere { ExerciseWords.id eq id }
            deletedRows > 0
        }
    } catch (e: Exception) {
        throw BadRequestException("Error deleting PhraseWord: ${e.message}")
    }
    fun deletePhraseWordsByPhraseId(phraseId: Int): Boolean = try {
        transaction {
            val deletedRows = ExerciseWords.deleteWhere { ExerciseWords.phraseId eq phraseId }
            deletedRows > 0
        }
    } catch (e: Exception) {
        throw BadRequestException("Error deleting PhraseWords by Phrase ID: ${e.message}")
    }
}