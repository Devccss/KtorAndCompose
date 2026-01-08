package com.example.services

import com.example.dtos.CreateUnitDto
import com.example.dtos.UnitCompletedDto
import com.example.dtos.UnitDto
import com.example.dtos.UpdateUnitDto
import models.DifficultyLevel
import repositories.UnitRepository

class UnitService(private val unitRepository: UnitRepository) {
    fun getAllUnits(): List<UnitDto> {
        return unitRepository.getAllUnits()
    }

    fun getUnitsByDifficulty(difficulty: DifficultyLevel): List<UnitDto> {
        return unitRepository.getUnitsByDifficulty(difficulty)
    }

    fun getUnitById(id: Int): UnitDto? {
        return unitRepository.getUnitById(id)
    }

    fun createUnit(dto: CreateUnitDto): UnitDto {
        return unitRepository.createUnit(dto)
    }

    fun updateUnit(id: Int, dto: UpdateUnitDto) {
        return unitRepository.updateUnit(id, dto)
    }
    fun deleteUnit(id: Int): Boolean {
        return unitRepository.deleteUnit(id)
    }

    fun getUnitsCompletedByUser(userId: Int): List<UnitCompletedDto> {
        return unitRepository.getUnitsCompletedByUser(userId)
    }

}