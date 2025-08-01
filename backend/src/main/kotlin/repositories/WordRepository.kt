package com.example.repositories

import models.Word
import com.example.dtos.CreateWordDto
import com.example.dtos.WordDto
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class WordRepository {
    fun createWord (dto: CreateWordDto): WordDto= transaction {
        val newWord = Word.insert {
            it[english] = dto.english
            it[spanish] = dto.spanish
            it[phonetic] = dto.phonetic
            it[description] = dto.description
            it[isActive] = true // Default value for new words
        }[Word.id]

        WordDto(
            id = newWord.value ,
            english = dto.english,
            spanish = dto.spanish,
            phonetic = dto.phonetic,
            description = dto.description,
            isActive = true
        )
    }
    fun getAllWords(): List<WordDto> = transaction {
        Word.selectAll().map { row ->
            WordDto(
                id = row[Word.id].value,
                english = row[Word.english],
                spanish = row[Word.spanish],
                phonetic = row[Word.phonetic],
                description = row[Word.description],
                isActive = row[Word.isActive]
            )
        }
    }
    fun getWordById(id: Int): WordDto? = transaction {
        Word.selectAll().where { Word.id eq id }.singleOrNull()?.let { row ->
            WordDto(
                id = row[Word.id].value,
                english = row[Word.english],
                spanish = row[Word.spanish],
                phonetic = row[Word.phonetic],
                description = row[Word.description],
                isActive = row[Word.isActive]
            )
        }
    }
    fun getWordsByPhraseId(phraseId: Int): List<WordDto> = transaction {
        Word.selectAll().where { Word.id eq phraseId }.map { row ->
            WordDto(
                id = row[Word.id].value,
                english = row[Word.english],
                spanish = row[Word.spanish],
                phonetic = row[Word.phonetic],
                description = row[Word.description],
                isActive = row[Word.isActive]
            )
        }
    }
    fun updateWord(id: Int, dto: CreateWordDto): WordDto? = transaction {
        Word.update({ Word.id eq id }) {
            it[english] = dto.english
            it[spanish] = dto.spanish
            it[phonetic] = dto.phonetic
            it[description] = dto.description
        }
        getWordById(id)
    }
    fun deleteWord(id: Int): Boolean = transaction {
        val deletedRows = Word.deleteWhere { Word.id eq id }
        deletedRows > 0
    }
}