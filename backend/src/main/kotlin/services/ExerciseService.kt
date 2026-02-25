package com.example.services

import com.example.dtos.CreateExerciseCompletedDto
import com.example.dtos.CreateExerciseDto
import com.example.dtos.ExerciseDto
import com.example.dtos.UpdateExerciseCompletedDto
import com.example.dtos.UpdateExerciseDto
import repositories.ExerciseRepository

class ExerciseService(private val repo: ExerciseRepository) {
    fun getAll(): List<ExerciseDto> = repo.getAll()
    fun getById(id: Int): ExerciseDto? = repo.getById(id)
    fun getByUnitId(unitId: Int): List<ExerciseDto> = repo.getByUnitId(unitId)
    fun create(dto: CreateExerciseDto): ExerciseDto = repo.create(dto)
    fun update(id: Int, dto: UpdateExerciseDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)

    fun createExerciseCompleted(dto: CreateExerciseCompletedDto) {
        repo.createExerciseCompleted(dto)
    }
    fun getAllExerciseCompleted() = repo.getAllExerciseCompleted()
    fun getExerciseCompletedById(id: Int) = repo.getExerciseCompletedById(id)
    fun getExerciseCompletedByUser(userId: Int) = repo.getExercisesCompletedByUser(userId)
    fun updateExerciseCompleted(id: Int, dto: UpdateExerciseCompletedDto) {
        repo.updateExerciseCompleted(id, dto)
    }
    fun reorderExercises(orders: List<Pair<Int, Int>>) = repo.reorderExercises(orders)
    fun deleteExerciseCompleted(id: Int) {
        repo.deleteExerciseCompleted(id)
    }
}
