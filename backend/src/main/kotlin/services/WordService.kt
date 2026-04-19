package com.example.services

import com.example.dtos.CreateWordDto
import com.example.dtos.FilterWordsDto
import com.example.dtos.UpdateWordDto
import com.example.dtos.WordDto
import repositories.WordRepository

class WordService(private val repo: WordRepository) {
    fun getAll(): List<WordDto> = repo.getAll()
    fun getById(id: Int): WordDto? = repo.getById(id)
    fun searchWords(filters: FilterWordsDto): List<WordDto> = repo.searchWords(filters)
    fun create(dto: CreateWordDto): WordDto = repo.create(dto)
    fun update(id: Int, dto: UpdateWordDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
