package com.example.services

import com.example.dtos.CreatePhraseDto
import com.example.dtos.PhraseDto
import com.example.dtos.OrderPhraseDto
import com.example.repositories.PhraseRepository
import services.NotFoundException
import services.ValidationException

class PhraseService(private val phraseRepository: PhraseRepository) {

    fun getAllPhrases(): List<PhraseDto> {
        return phraseRepository.getAllPhrases()
    }

    fun getPhraseById(id: Int): PhraseDto {
        return phraseRepository.getPhraseById(id) ?: throw NotFoundException("Phrase not found")
    }

    fun getPhrasesByParticipantId(participantId: Int): List<PhraseDto> {
        return phraseRepository.getPhrasesByParticipantId(participantId)
    }
    fun createPhrase(phrase: CreatePhraseDto, participantID: Int): PhraseDto {
        validatePhrase(phrase)
        return phraseRepository.createPhrase(
            phrase,
            participantID
        )
    }
    fun cretePhraseOrder(order: OrderPhraseDto): OrderPhraseDto {
        return phraseRepository.createPhraseOrder(order)
    }
    fun updatePhrase(id: Int, phrase: CreatePhraseDto): PhraseDto {
        validatePhrase(phrase)
        phraseRepository.updatePhrase(id, phrase)
        return phraseRepository.getPhraseById(id) ?: throw NotFoundException("Phrase not found")
    }

    fun deletePhrase(id: Int): Boolean {
        return phraseRepository.deletePhrase(id)
    }

    private fun validatePhrase(phrase: CreatePhraseDto) {
        if (phrase.englishText.isBlank()) {
            throw ValidationException("La frase en inglés no puede estar vacía")
        }
    }
    fun getOrderPhraseByPhraseId(phraseId: Int): Int? {
        return phraseRepository.getOrderPhraseByPhraseId(phraseId)
    }
    fun orderPhrase(dto:OrderPhraseDto): Boolean {
        return phraseRepository.orderPhrases(dto)
    }
    fun deleteOrderPhraseByPhraseId(phraseId: Int): Boolean {
        return phraseRepository.deleteOrderPhraseByPhraseId(phraseId)
    }

}