package com.example.EcomSphere.Services.AuthService

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
class AuthController(private val auth: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Unit> =
        runCatching { ResponseEntity.ok(auth.register(req)) }
            .getOrElse { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthResponse> =
        runCatching { ResponseEntity.ok(auth.login(req)) }
            .getOrElse { throw ResponseStatusException(HttpStatus.UNAUTHORIZED, it.message) }
}