package config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class TokenManager(
    private val secret: String,
    private val issuer: String,
    private val audience: String
) {
    fun generateToken(id: Int, email: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id",id)
            .withClaim("username", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 6000000))
            .sign(Algorithm.HMAC256(secret))
    }
}
