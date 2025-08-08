package com.example.repositories

import models.PhraseWords
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
            val phraseWord = PhraseWords.insert {
                it[phraseId] = dto.phraseId
                it[wordId] = dto.wordId
                it[order] = dto.order
            }[PhraseWords.id]

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
            PhraseWords.selectAll().map { row ->
                PhraseWordDto(
                    id = row[PhraseWords.id].value,
                    phraseId = row[PhraseWords.phraseId],
                    wordId = row[PhraseWords.wordId],
                    select = row[PhraseWords.selectToTranslate],
                    order = row[PhraseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWords: ${e.message}")
    }

    fun getPhraseWordById(id: Int): PhraseWordDto? = try {
        transaction {
            PhraseWords.selectAll().where { PhraseWords.id eq id }.singleOrNull()?.let { row ->
                PhraseWordDto(
                    id = row[PhraseWords.id].value,
                    phraseId = row[PhraseWords.phraseId],
                    wordId = row[PhraseWords.wordId],
                    order = row[PhraseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWord by ID: ${e.message}")
    }

    fun getPhraseWordsByPhraseId(phraseId: Int): List<PhraseWordDto> = try {
        transaction {
            PhraseWords.selectAll().where { PhraseWords.phraseId eq phraseId }.map { row ->
                PhraseWordDto(
                    id = row[PhraseWords.id].value,
                    phraseId = row[PhraseWords.phraseId],
                    wordId = row[PhraseWords.wordId],
                    order = row[PhraseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWords by Phrase ID: ${e.message}")
    }


    fun getPhraseWordsByWordId(wordId: Int): List<PhraseWordDto> = try {
        transaction {
            PhraseWords.selectAll().where { PhraseWords.wordId eq wordId }.map { row ->
                PhraseWordDto(
                    id = row[PhraseWords.id].value,
                    phraseId = row[PhraseWords.phraseId],
                    wordId = row[PhraseWords.wordId],
                    order = row[PhraseWords.order]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching PhraseWords by Word ID: ${e.message}")
    }

    fun updatePhraseWord(id: Int, dto: CreatePhraseWordDto): PhraseWordDto? = try {
        transaction {
            PhraseWords.update({ PhraseWords.id eq id }) {
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
            val deletedRows = PhraseWords.deleteWhere { PhraseWords.id eq id }
            deletedRows > 0
        }
    } catch (e: Exception) {
        throw BadRequestException("Error deleting PhraseWord: ${e.message}")
    }
    fun deletePhraseWordsByPhraseId(phraseId: Int): Boolean = try {
        transaction {
            val deletedRows = PhraseWords.deleteWhere { PhraseWords.phraseId eq phraseId }
            deletedRows > 0
        }
    } catch (e: Exception) {
        throw BadRequestException("Error deleting PhraseWords by Phrase ID: ${e.message}")
    }
}