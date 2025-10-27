package com.example.services

import com.example.dtos.CreateTestDto
import com.example.dtos.TestDto
import com.example.repositories.TestRepository
import services.ValidationException

class TestService(private val testRepository: TestRepository) {
    fun addDialogTest(dialogId: Int, testId: Int): Boolean {
        return testRepository.addDialogToTest(dialogId,testId)
    }
    fun createTest(test: CreateTestDto, levelId:Int): TestDto {
        validateTestCreation(test)
        return testRepository.createTest(test, levelId)
    }
    fun editTest(testId: Int, test: CreateTestDto): Boolean {
        validateTestCreation(test)
        return testRepository.editTest(testId, test)
    }
    fun getTestById(testId: Int): TestDto? {
        return testRepository.getTestById(testId)
    }
    fun getAllTests(): List<TestDto> {
        return testRepository.getAllTests()
    }
    fun deleteTest(testId: Int): Boolean {
        return testRepository.deleteTest(testId)
    }
    fun deleteDialogTest(dialogId: Int,testId: Int): Boolean {
        return testRepository.deleteDialogTest(dialogId,testId)
    }

    private fun validateTestCreation(test: CreateTestDto) {
        if (test.name.isBlank()) {
            throw ValidationException("El nombre del test no puede estar vacío")
        }
        if (test.description.isBlank()) {
            throw ValidationException("La descripción del test no puede estar vacía")
        }

    }
}