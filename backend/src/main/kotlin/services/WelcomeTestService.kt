package com.example.services

import com.example.dtos.CreateWelcomeTestDto
import com.example.dtos.UpdateWelcomeTestDto
import com.example.repositories.WelcomeTestRepo

class WelcomeTestService(private val repo: WelcomeTestRepo) {
    fun getAll() = repo.getAll()
    fun getByTestId(testId: Int) = repo.getByTestId(testId)
    fun createWelcomeTest(dto: CreateWelcomeTestDto) = repo.createWelcomeTest(dto)
    fun updateWelcomeTest(id: Int, dto: UpdateWelcomeTestDto) = repo.updateWelcomeTest(id, dto)
    fun deleteByTestId(testId: Int) = repo.deleteByTestId(testId)
}