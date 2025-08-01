package com.example.services

import com.example.dtos.CreateWordDto
import com.example.dtos.WordDto
import com.example.repositories.WordRepository

class WordService(private val wordRepository: WordRepository) {
    fun createWord(dto: CreateWordDto): WordDto {
        return wordRepository.createWord(dto)
    }

    fun getAllWords(): List<WordDto> {
        return wordRepository.getAllWords()
    }

    fun getWordById(id: Int): WordDto? {
        return wordRepository.getWordById(id)
    }

    fun getWordsByPhraseId(phraseId: Int): List<WordDto> {
        return wordRepository.getWordsByPhraseId(phraseId)
    }
    fun updateWord(id: Int, dto: CreateWordDto): WordDto? {
        return wordRepository.updateWord(id, dto)
    }
    fun deleteWord(id: Int): Boolean {
        return wordRepository.deleteWord(id)
    }
}