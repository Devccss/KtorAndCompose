package services

import com.example.dtos.CreateUnitCompletedDto
import com.example.dtos.CreateUnitDto
import com.example.dtos.FilterUnitsDto
import com.example.dtos.UnitCompletedDto
import com.example.dtos.UnitDto
import com.example.dtos.UpdateUnitCompletedDto
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

    fun getUnitByTestId(testId: Int): UnitDto? {
        return unitRepository.getUnitByTestId(testId)
    }

    fun searchUnits(filters: FilterUnitsDto): List<UnitDto> {
        return unitRepository.searchUnits(filters)
    }

    fun createUnit(dto: CreateUnitDto): UnitDto {
        return unitRepository.createUnit(dto)
    }

    fun updateUnit(id: Int, dto: UpdateUnitDto) {
        return unitRepository.updateUnit(id, dto)
    }
    fun reorderUnits(units: List<Pair<Int, Int>>): Boolean{
        return unitRepository.reorderUnits(units)
    }

    fun deleteUnit(id: Int): Boolean {
        return unitRepository.deleteUnit(id)
    }

    fun getUnitsCompletedByUser(userId: Int): List<UnitDto> {
        return unitRepository.getUnitsCompletedByUser(userId)
    }

    fun getAllUnitsCompletedByStudents(): List<UnitCompletedDto> {
        return unitRepository.getAllUnitsCompletedByStudents()
    }


    fun createUnitCompleted(dto: CreateUnitCompletedDto): UnitCompletedDto =
        unitRepository.createUnitsCompleted(dto)

    fun editUnitsCompleted (completedId: Int, dto: UpdateUnitCompletedDto) =
        unitRepository.editUnitsCompleted(completedId, dto)

    fun getAllUnitsCompletedByUser(userId: Int): List<UnitDto> =
        unitRepository.getUnitsCompletedByUser(userId)
    fun getUnitCompletedById(completedId: Int): UnitDto? =
        unitRepository.getUnitsCompletedById(completedId)

    fun getAllUnitsCompleted(): List<UnitDto> =
        unitRepository.getAllUnitsCompleted()

    fun deleteUnitsCompleted(completedId: Int) =
        unitRepository.deleteUnitsCompleted(completedId)

    fun deleteUnitsCompletedByUserId( unitId: Int) =
        unitRepository.deleteUnitsCompletedByUserId( unitId)
}