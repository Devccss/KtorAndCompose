package com.example.services

import com.example.dtos.CreateUserDto
import com.example.dtos.FilterUsersDto
import com.example.dtos.LoginDto

import com.example.dtos.UpdateUserDto
import com.example.dtos.UserDto
import config.TokenManager
import repositories.UsersRepository

class UserService(
    private val userRepository: UsersRepository,
    private val tokenManager: TokenManager
) {
    fun initSession(dto: LoginDto): UserDto {
        val user = userRepository.login(dto.email, dto.password)
        val token = tokenManager.generateToken(user.id ,user.email, user.role.name)
        return user.copy(token = token)
    }

    fun createUser(dto: CreateUserDto): UserDto {
        return userRepository.createUser(dto)
    }

    fun getAllUsers(): List<UserDto> {
        return userRepository.getAll()
    }
    fun getUserById(id: Int): UserDto? {
        return userRepository.getById(id)
    }
    fun getUserByEmail(email: String): UserDto? {
        return userRepository.getByEmail(email)
    }
    fun getUsersByName(name: String): List<UserDto> {
        return userRepository.getUsersByName(name)
    }
    fun getFilterUsers(filters:FilterUsersDto): List<UserDto> {
        return userRepository.getFilterUsers(filters)
    }
    fun updateUser(id: Int, dto: UpdateUserDto) {
        return userRepository.updateUser(id, dto)
    }
    fun deleteUser(id: Int): Boolean {
        return userRepository.deleteUser(id)
    }

}
