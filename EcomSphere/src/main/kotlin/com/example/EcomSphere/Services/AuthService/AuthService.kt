package com.example.EcomSphere.Services.AuthService

import com.example.EcomSphere.JwtUtil
import com.example.EcomSphere.Services.UserService.User
import com.example.EcomSphere.Services.UserService.UserRepository
import io.netty.handler.codec.http.HttpResponse
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val repo: UserRepository,
    private val jwt: JwtUtil
) {
    private val bcrypt = BCryptPasswordEncoder()

    fun register(req: RegisterRequest) {
        val email = req.email.lowercase()
        if (repo.existsByEmail(email)) throw IllegalArgumentException("Email already in use")
        val saved = repo.save(
            User(
                email = email,
                name = req.name,
                passwordHash = bcrypt.encode(req.password)
            )
        )
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = repo.findByEmail(req.email.lowercase())
            .orElseThrow { IllegalArgumentException("Invalid credentials") }
        if (!bcrypt.matches(req.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }
        return AuthResponse(jwt.generate(user.email))
    }
}