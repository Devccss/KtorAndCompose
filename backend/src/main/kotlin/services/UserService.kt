package com.example.services

import com.example.dtos.CreateUserDto
import com.example.dtos.LoginDto
import com.example.dtos.UsersDto
import com.example.repositories.UsersRepository

class UserService(private val userRepository: UsersRepository) {
    fun initSesion(dto: LoginDto): UsersDto {
        return userRepository.initSesion(dto)
    }

    fun createUser(dto: CreateUserDto): UsersDto {
        return userRepository.createUser(dto)
    }
    fun getAllUsers(): List<UsersDto?> {
        return userRepository.getAllUsers()
    }
    fun getUserById(id: Int): UsersDto? {
        return userRepository.getUserById(id)
    }
    fun getUserByEmail(email: String): UsersDto? {
        return userRepository.getUserByEmail(email)
    }
    fun updateUser(id: Int, dto: CreateUserDto): UsersDto? {
        return userRepository.updateUser(id, dto)
    }
    fun deleteUser(id: Int): Boolean {
        return userRepository.deleteUser(id)
    }

}