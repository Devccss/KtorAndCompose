
import models.DifficultyLevel
import kotlinx.serialization.Serializable

@Serializable
data class LevelDTO(
    val id: Int,
    val accent: Int,
    val difficulty: DifficultyLevel,
    val name: String,
    val description: String,
    val orderLevel: Float,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

@Serializable
data class LevelCreationDTO(
    val difficulty: DifficultyLevel,
    val name: String,
    val description: String,
    val orderLevel: Float? = null,


)

@Serializable
data class LevelUpdateDTO(
    val accent: Int? = null,
    val difficulty: DifficultyLevel? = null,
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null,
    val orderLevel: Float? = null
)