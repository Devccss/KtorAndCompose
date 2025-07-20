package org.example.project.repository.levelRepository

import org.example.project.models.Level


interface LevelRepository {
    suspend fun addLevel(level: Level, beforeId: Int? = null, afterId: Int? = null): Level
    suspend fun getAllLevels(): List<Level>
    suspend fun getLevelById(id: Int): Level?
    suspend fun updateLevel(id: Int, level: Level, beforeId: Int?, afterId: Int?): Level
    suspend fun deleteLevel(id: Int): Result<Unit>
    suspend fun getLevelsByDifficulty(difficulty: String): List<Level>
}
