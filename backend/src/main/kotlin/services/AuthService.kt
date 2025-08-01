package com.example.services

import com.example.dtos.UsersDto
import com.example.repositories.UsersRepository
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.dtos.GoogleUserDto
import java.util.*

class AuthService(
    private val usersRepository: UsersRepository,
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String
) {
    fun loginWithGoogle(googleToken: String,dto:GoogleUserDto): String? {
        val userInfo = getGoogleUserInfo(googleToken)
        val user = usersRepository.findOrCreateByEmail(userInfo.email, dto)
        return user?.let { generateJwtToken(it) }
    }

    private fun getGoogleUserInfo(googleToken: String): UsersDto {
        // Implementa aquí la llamada HTTP a Google para obtener la info del usuario
        // y retorna un UsersDto con los datos.
        TODO("Implementar obtención de datos de Google")
    }

    private fun generateJwtToken(user: UsersDto): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", user.id)
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + 36_000_00 * 24)) // 24 horas
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}