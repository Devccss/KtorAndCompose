package com.example.repositories

import com.example.dtos.CreateUserDto
import com.example.dtos.GoogleUserDto
import com.example.dtos.LoginDto
import com.example.dtos.UsersDto
import io.ktor.server.plugins.BadRequestException
import models.Role
import models.Users
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

class UsersRepository {
    fun findOrCreateByEmail(email: String, dto: GoogleUserDto): UsersDto? = try {
        transaction {
            Users.selectAll().where { Users.email eq email }.singleOrNull()?.let { row ->
                UsersDto(
                    id = row[Users.id].value,
                    name = row[Users.name],
                    email = row[Users.email],
                    preferences = row[Users.preferences],
                    password = row[Users.password],
                    provider = row[Users.provider],
                    providerId = row[Users.providerId].toString(),
                    currentLevelId = row[Users.currentLevelId],
                    createdAt = row[Users.createdAt].toString(),
                    role = row[Users.role]
                )
            } ?: createGoogleUser(dto)
        }
    } catch (e: Exception) {
        throw BadRequestException("Error finding or creating user: ${e.message}")

    }

    fun initSesion(dto:LoginDto): UsersDto = try {
        transaction {
            Users.selectAll().where { Users.email eq dto.email }.singleOrNull()?.let { row ->
                if (BCrypt.checkpw(dto.password, row[Users.password])) {
                    UsersDto(
                        id = row[Users.id].value,
                        name = row[Users.name],
                        email = row[Users.email],
                        preferences = row[Users.preferences],
                        provider = row[Users.provider],
                        providerId = row[Users.providerId].toString(),
                        currentLevelId = row[Users.currentLevelId],
                        createdAt = row[Users.createdAt].toString(),
                        role = row[Users.role]
                    )
                } else {
                    throw BadRequestException("Invalid credentials")
                }
            } ?: throw BadRequestException("Invalid email or user not found")
        }

    } catch (e: Exception) {
        throw BadRequestException("Error initializing session: ${e.message}")
    }

    private fun createGoogleUser(dto: GoogleUserDto): UsersDto? = try {
        transaction {
            val newUser = Users.insert {
                it[name] = dto.name
                it[email] = dto.email
                it[preferences] = dto.preferences
                it[provider] = dto.provider ?: "google"
                it[providerId] = dto.providerId ?: "google-${dto.email}"
                it[currentLevelId] = dto.currentLevelId ?: 0

            }[Users.id]

            UsersDto(
                id = newUser.value,
                name = dto.name,
                email = dto.email,
                preferences = dto.preferences,
                currentLevelId = dto.currentLevelId,
                createdAt = LocalDateTime.now().toString(),
                role = Role.STUDENT
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error creating Google user: ${e.message}")
    }

    fun createUser(dto: CreateUserDto): UsersDto = try {
        transaction {
            val hashedPassword = hashPassword(dto.password)
            val newUser = Users.insert {
                it[name] = dto.name
                it[email] = dto.email
                it[password] = hashedPassword
                it[preferences] = dto.preferences
                it[provider] = dto.provider ?: "local"
                it[providerId] = dto.providerId ?: "local-${dto.email}"
                it[currentLevelId] = dto.currentLevelId ?: 0
                it[role] = dto.role ?: Role.STUDENT
            }[Users.id]

            UsersDto(
                id = newUser.value,
                name = dto.name,
                email = dto.email,
                preferences = dto.preferences,
                provider = dto.provider?: "local",
                providerId = dto.providerId ?: "local-${dto.email}",
                currentLevelId = dto.currentLevelId,
                createdAt = LocalDateTime.now().toString(),
                role = dto.role ?: Role.STUDENT
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error creating user: ${e.message}")
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun getAllUsers(): List<UsersDto?> = try {
        transaction {
            Users.selectAll().map { row ->
                UsersDto(
                    id = row[Users.id].value,
                    name = row[Users.name],
                    email = row[Users.email],
                    preferences = row[Users.preferences],
                    currentLevelId = row[Users.currentLevelId],
                    createdAt = row[Users.createdAt].toString(),
                    role = row[Users.role]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching users: ${e.message}")
    }

    fun getUserById(id: Int): UsersDto? = try {
        transaction {
            Users.selectAll().where { Users.id eq id }.singleOrNull()?.let { row ->
                UsersDto(
                    id = row[Users.id].value,
                    name = row[Users.name],
                    email = row[Users.email],
                    preferences = row[Users.preferences],
                    currentLevelId = row[Users.currentLevelId],
                    createdAt = row[Users.createdAt].toString(),
                    role = row[Users.role]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching user by ID: ${e.message}")
    }
    fun getUserByEmail(email: String): UsersDto? = try {
        transaction {
            Users.selectAll().where { Users.email eq email }.singleOrNull()?.let { row ->
                UsersDto(
                    id = row[Users.id].value,
                    name = row[Users.name],
                    email = row[Users.email],
                    preferences = row[Users.preferences],
                    currentLevelId = row[Users.currentLevelId],
                    createdAt = row[Users.createdAt].toString(),
                    role = row[Users.role]
                )
            }
        }
    } catch (e: Exception) {
        throw BadRequestException("Error fetching user by email: ${e.message}")
    }
    fun updateUser(id: Int, dto: CreateUserDto): UsersDto? = try {
        transaction {
            Users.update({ Users.id eq id }) { update ->
                update[name] = dto.name
                update[email] = dto.email
                update[password] = dto.password
                update[preferences] = dto.preferences
                update[currentLevelId] = dto.currentLevelId ?: 0
            }

            getUserById(id)
        }
    } catch (e: Exception) {
        throw BadRequestException("Error updating user: ${e.message}")
    }

    fun deleteUser(id: Int): Boolean = try {
        transaction {
            Users.deleteWhere { Users.id eq id } > 0
        }
    } catch (e: Exception) {
        throw BadRequestException("Error deleting user: ${e.message}")
    }
}