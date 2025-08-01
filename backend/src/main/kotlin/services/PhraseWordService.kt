package com.example.services
import com.example.dtos.CreatePhraseWordDto
import com.example.dtos.PhraseWordDto
import com.example.repositories.PhraseWordRepository

class PhraseWordService(private val phraseWordRepository: PhraseWordRepository) {

    fun createPhraseWord(dto: CreatePhraseWordDto): PhraseWordDto {
        return phraseWordRepository.createPhraseWord(dto)
    }

    fun getAllPhraseWords(): List<PhraseWordDto> {
        return phraseWordRepository.getAllPhraseWords()
    }

    fun getPhraseWordById(id: Int): PhraseWordDto? {
        return phraseWordRepository.getPhraseWordById(id)
    }

    fun getPhraseWordsByPhraseId(phraseId: Int): List<PhraseWordDto> {
        return phraseWordRepository.getPhraseWordsByPhraseId(phraseId)
    }

    fun getPhraseWordsByWordId(wordId: Int): List<PhraseWordDto> {
        return phraseWordRepository.getPhraseWordsByWordId(wordId)
    }
    fun updatePhraseWord(id: Int, dto: CreatePhraseWordDto): PhraseWordDto? {
        return phraseWordRepository.updatePhraseWord(id, dto)
    }
    fun deletePhraseWord(id: Int): Boolean {
        return phraseWordRepository.deletePhraseWord(id)
    }
}