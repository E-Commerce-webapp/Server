package com.example.EcomSphere.Services.UserService

import com.example.EcomSphere.Helper.CustomUserPrincipal
import com.example.EcomSphere.MiddleWare.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService,
    private val jwtUtil: JwtUtil
){
    @GetMapping("/become-seller")
    fun becomeSellerHandle(request: HttpServletRequest): ResponseEntity<String> {
        val authHeader = request.getHeader("Authorization")
            ?: return ResponseEntity.status(401).body("Missing Authorization header")

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid Authorization header")
        }

        val email = jwtUtil.verifyAndGetEmail(token)
            ?: return ResponseEntity.status(401).body("Invalid or expired token")

        emailService.sendVerificationEmail(email, token)

        return ResponseEntity.ok("Verification email sent.")
    }

    @GetMapping("")
    fun findUserByEmail(authentication: Authentication): ResponseEntity<GetUsersResponse>{
        val principal = authentication.principal as CustomUserPrincipal
        val id = principal.id
        val user = userService.findUserById(id)
        return ResponseEntity.ok(user)
    }
}
