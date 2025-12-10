package com.example.EcomSphere.Services.UserService

import com.example.EcomSphere.Helper.CustomUserPrincipal
import com.example.EcomSphere.MiddleWare.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {
    @PostMapping("/submit-kyc")
    fun submitKyc(
        authentication: Authentication,
        @RequestBody kycData: KycSubmissionRequest
    ): ResponseEntity<Map<String, String>> {
        val principal = authentication.principal as CustomUserPrincipal
        val userId = principal.id
        
        userService.submitKyc(userId, kycData)
        
        return ResponseEntity.ok(mapOf(
            "message" to "Verification email sent. Please check your email to complete the seller registration."
        ))
    }
    
    @PostMapping("/verify-seller")
    fun verifySeller(
        @RequestParam token: String
    ): ResponseEntity<KycVerificationResponse> {
        val result = userService.verifySeller(token)
        if (result.success) {
            // In a real app, you would get the user ID from the token and update their status
            // For now, we'll just return the success message
            return ResponseEntity.ok(result)
        }
        return ResponseEntity.badRequest().body(result)
    }

    @GetMapping("")
    fun getUserInfo(authentication: Authentication): ResponseEntity<GetUsersResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val user = userService.findUserById(principal.id)
        return ResponseEntity.ok(user)
    }
}
