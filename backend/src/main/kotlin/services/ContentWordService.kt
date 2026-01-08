package com.example.services

import com.example.dtos.ContentWordDto
import com.example.dtos.CreateContentWordDto
import com.example.dtos.UpdateContentWordDto
import repositories.ContentWordRepository

class ContentWordService(private val repo: ContentWordRepository) {
    fun getAll(): List<ContentWordDto> = repo.getAll()
    fun getById(id: Int): ContentWordDto? = repo.getById(id)
    fun create(dto: CreateContentWordDto): ContentWordDto = repo.create(dto)
    fun update(id: Int, dto: UpdateContentWordDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
