package com.example.repositories

import models.Phrase
import models.PhraseOrder
import com.example.dtos.CreatePhraseDto
import com.example.dtos.PhraseDto
import com.example.dtos.OrderPhraseDto
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class PhraseRepository {
    fun createPhrase(dto:CreatePhraseDto,participantID: Int): PhraseDto = transaction {
        val jsonSpanish = Json.encodeToString(dto.spanishText)
        val newPhrase = Phrase.insert {
            it[participantId] = participantID
            it[englishText] = dto.englishText
            it[spanishText] = jsonSpanish
            it[isActive] = true
            it[createdAt] = LocalDateTime.now()
        }[Phrase.id]

        PhraseDto(
            id = newPhrase.value,
            participantId = participantID,
            audioUrl = dto.audioUrl,
            englishText = dto.englishText,
            spanishText = dto.spanishText,
            isActive = true,
            createdAt = LocalDateTime.now().toString(),
        )
    }
    fun createPhraseOrder(dto: OrderPhraseDto): OrderPhraseDto = transaction {
        val newOrder = PhraseOrder.insert {
            it[dialogId] = dto.dialogId
            it[phraseId] = dto.phraseId
            it[order] = dto.order
        }[PhraseOrder.id]
        OrderPhraseDto(
            dialogId = dto.dialogId,
            phraseId = dto.phraseId,
            order = dto.order
        )
    }
    fun getAllPhrases(): List<PhraseDto> = transaction {
        Phrase.selectAll().map { row ->
            PhraseDto(
                id = row[Phrase.id].value,
                participantId = row[Phrase.participantId],
                audioUrl = row[Phrase.audioUrl],
                englishText = row[Phrase.englishText],
                spanishText = row[Phrase.spanishText]?.let {
                    Json.decodeFromString(it)
                },
                isActive = row[Phrase.isActive],
                createdAt = row[Phrase.createdAt].toString()
            )
        }
    }
    fun getPhrasesByParticipantId(participantId: Int): List<PhraseDto> = transaction {
        Phrase.selectAll().where { Phrase.participantId eq participantId }.map { row ->
            PhraseDto(
                id = row[Phrase.id].value,
                participantId = row[Phrase.participantId],
                audioUrl = row[Phrase.audioUrl],
                englishText = row[Phrase.englishText],
                spanishText = row[Phrase.spanishText]?.let {
                    Json.decodeFromString(it)
                },
                isActive = row[Phrase.isActive],
                createdAt = row[Phrase.createdAt].toString()
            )
        }
    }
    fun getPhraseById(id: Int): PhraseDto? = transaction {
        Phrase.selectAll() .where { Phrase.id eq id }.singleOrNull()?.let { row ->
            PhraseDto(
                id = row[Phrase.id].value,
                participantId = row[Phrase.participantId],
                audioUrl = row[Phrase.audioUrl],
                englishText = row[Phrase.englishText],
                spanishText = row[Phrase.spanishText]?.let {
                    Json.decodeFromString(
                        it
                    )
                },
                isActive = row[Phrase.isActive],
                createdAt = row[Phrase.createdAt].toString()
            )
        }
    }
    fun getOrderPhraseByPhraseId(phraseId: Int): Int? = transaction {
        PhraseOrder.selectAll()
            .where { PhraseOrder.phraseId eq phraseId }
            .singleOrNull()?.get(PhraseOrder.order)
    }

    fun updatePhrase(id: Int, dto: CreatePhraseDto): PhraseDto? = transaction {
        val jsonSpanish = Json.encodeToString(dto.spanishText)
        Phrase.update({ Phrase.id eq id }) {
            it[englishText] = dto.englishText
            it[spanishText] = jsonSpanish
            it[audioUrl] = dto.audioUrl
        }
        getPhraseById(id)
    }

    fun deletePhrase(id: Int): Boolean = transaction {
        getPhraseById(id)?: throw IllegalArgumentException("Phrase with id $id not found")
        val deletedRows = Phrase.deleteWhere { Phrase.id eq id }
        deletedRows > 0
    }

    fun orderPhrases(dto: OrderPhraseDto): Boolean = transaction {
        val updatedRows = PhraseOrder.update({ PhraseOrder.dialogId eq dto.dialogId and (PhraseOrder.phraseId eq dto.phraseId) }) {
            it[order] = dto.order
        }
        updatedRows > 0
    }

}