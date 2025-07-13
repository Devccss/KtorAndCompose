package services

import DifficultyLevel
import LevelCreationDTO
import LevelDTO
import repositories.LevelRepository
import LevelUpdateDTO



class LevelService(private val levelRepository: LevelRepository) {

    fun getAllLevels(): List<LevelDTO> {
        return levelRepository.getAllLevels()
    }

    fun getLevelById(id: Int): LevelDTO {
        return levelRepository.getLevelById(id) ?: throw NotFoundException("Level not found")
    }

    fun createLevel(
        level: LevelCreationDTO,
        beforeId: Int? = null,
        afterId: Int? = null
    ): LevelDTO {
        validateLevelCreation(level)

        return levelRepository.createLevelSmart(level, beforeId, afterId)
    }


    fun updateLevel(id: Int, level: LevelUpdateDTO): LevelDTO {
        val updateResult = levelRepository.updateLevel(id, level)
        if (!updateResult) {
            throw NotFoundException("Level not found")
        }
        return levelRepository.getLevelById(id)!!
    }

    fun deleteLevel(id: Int): Boolean {
        return levelRepository.deleteLevel(id)
    }

     fun getLevelsByDifficulty(difficulty: DifficultyLevel): List<LevelDTO> {
        return levelRepository.getLevelsByDifficulty(difficulty)
    }

    private fun validateLevelCreation(level: LevelCreationDTO) {
        if (level.name.isBlank()) {
            throw ValidationException("Level name cannot be empty")
        }
        if (level.description.isBlank()) {
            throw ValidationException("Level description cannot be empty")
        }
    }
}


class NotFoundException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message)