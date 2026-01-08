package repositories

import com.example.dtos.CreateUserDto
import com.example.dtos.UpdateUserDto
import com.example.dtos.UserDto
import io.ktor.server.plugins.BadRequestException
import models.Users

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

class UsersRepository {

    private fun resultRowToUser(row: ResultRow): UserDto {
        return UserDto(
            id = row[Users.id].value,
            email = row[Users.email],
            name = row[Users.name],
            password = "" , // no devolver contraseña
            activeNow = row[Users.activeNow],
            currentUnitId = row[Users.currentUnitId],
            preferences = row[Users.preferences] ,
            provider = row[Users.provider],
            role = row[Users.role],
            createdAt = row[Users.createdAt].toString()
        )
    }

    fun getAll(): List<UserDto> = transaction {
        Users.selectAll().orderBy(Users.createdAt).map(::resultRowToUser)
    }

    fun getById(id: Int): UserDto? = transaction {
        Users.selectAll().where { Users.id eq id }.singleOrNull()?.let(::resultRowToUser)
    }

    fun getByEmail(email: String): UserDto? = transaction {
        Users.select ( Users.email eq email ).singleOrNull()?.let(::resultRowToUser)
    }

    fun createUser(dto: CreateUserDto): UserDto = try {
        transaction {
            val hashed = BCrypt.hashpw(dto.password, BCrypt.gensalt())
            val newId = Users.insert {
                it[email] = dto.email
                it[password] = hashed
                it[name] = dto.name
                it[preferences] = dto.preferences
                it[provider] = dto.provider
                it[currentUnitId] = dto.currentUnitId
                it[role] = dto.role ?: models.Role.STUDENT
            }[Users.id]

            UserDto(
                id = newId.value,
                email = dto.email,
                password = "",
                name = dto.name,
                preferences = dto.preferences,
                provider = dto.provider,
                currentUnitId = dto.currentUnitId,
                activeNow = false,
                role = dto.role ?: models.Role.STUDENT,
                createdAt = LocalDateTime.now().toString(),
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear usuario: ${e.message}")
    }

    fun updateUser(id: Int, dto: UpdateUserDto) {
        transaction {
            getById(id) ?: throw BadRequestException("Usuario con ID $id no existe.")
            Users.update({ Users.id eq id }) { u ->
                dto.email?.let { u[email] = it }
                dto.password?.let { newPass ->
                    u[password] = BCrypt.hashpw(newPass, BCrypt.gensalt())
                }
                dto.name?.let { u[name] = it }
                dto.preferences?.let { u[preferences] = it }
                dto.provider?.let { u[provider] = it }
                dto.currentUnitId?.let { u[currentUnitId] = it }
                dto.role?.let { u[role] = it}

            }
        }
    }

    fun deleteUser(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("Usuario con ID $id no existe.")
        Users.deleteWhere { Users.id eq id } > 0
    }

    fun login(email: String, password: String): UserDto = transaction {
        val row = Users.select ( Users.email eq email ).singleOrNull()
            ?: throw BadRequestException("Email o contraseña inválidos.")

        val hashed = row[Users.password]
        if (!BCrypt.checkpw(password, hashed)) {
            throw BadRequestException("Email o contraseña inválidos.")
        }

        // Devolver usuario sin contraseña
        resultRowToUser(row)
    }
}
