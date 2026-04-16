package com.example.services

import com.example.dtos.CreateExerciseCompletedDto
import com.example.dtos.CreateExerciseDto
import com.example.dtos.ExerciseCompletedDto
import com.example.dtos.ExerciseDto
import com.example.dtos.FilterExercisesDto
import com.example.dtos.UpdateExerciseCompletedDto
import com.example.dtos.UpdateExerciseDto
import repositories.ExerciseRepository

class ExerciseService(private val repo: ExerciseRepository) {
    fun getAll(): List<ExerciseDto> = repo.getAll()
    fun getById(id: Int): ExerciseDto? = repo.getById(id)
    fun searchExercises(filters:FilterExercisesDto) = repo.searchExercises(filters)
    fun getByUnitId(unitId: Int): List<ExerciseDto> = repo.getByUnitId(unitId)
    fun create(dto: CreateExerciseDto): ExerciseDto = repo.create(dto)
    fun update(id: Int, dto: UpdateExerciseDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)

    fun createExerciseCompleted(dto: CreateExerciseCompletedDto): ExerciseCompletedDto =
        repo.createExerciseCompleted(dto)

    fun getAllExerciseCompleted(): List<ExerciseCompletedDto> = repo.getAllExerciseCompleted()
    fun getExerciseCompletedById(id: Int): ExerciseCompletedDto? = repo.getExerciseCompletedById(id)
    fun getExerciseCompletedByUser(userId: Int): List<ExerciseCompletedDto>  = repo.getExercisesCompletedByUser(userId)
    fun updateExerciseCompleted(id: Int, dto: UpdateExerciseCompletedDto) =
        repo.updateExerciseCompleted(id, dto)

    fun reorderExercises(orders: List<Pair<Int, Int>>) = repo.reorderExercises(orders)
    fun deleteExerciseCompleted(id: Int) {
        repo.deleteExerciseCompleted(id)
    }
}
