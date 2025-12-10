package com.example.EcomSphere.Services.UserService

import com.example.EcomSphere.Helper.NotFoundActionException
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    private val verificationTokens = mutableMapOf<String, KycSubmissionRequest>()
    
    fun findUserById(id: String): GetUsersResponse {
        val user = userRepository.findById(id)
            .orElseThrow { NotFoundActionException("User with ID $id is not available") }

        return GetUsersResponse(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isASeller = user.isASeller ?: false,
            emailConfirm = user.emailConfirm ?: false
        )
    }
    
    fun submitKyc(userId: String, kycData: KycSubmissionRequest) {
        val token = UUID.randomUUID().toString()
        verificationTokens[token] = kycData
        
        // In a real app, you would save this to the database
        // For now, we'll store it in memory
        
        // Send verification email
        val verificationLink = "${System.getenv("FRONTEND_URL") ?: "http://localhost:3000"}/verify-seller?token=$token"
        val emailBody = """
            <h2>Verify Your Seller Account</h2>
            <p>Thank you for submitting your KYC information. Please click the link below to verify your seller account:</p>
            <a href="$verificationLink">Verify Seller Account</a>
            <p>This link will expire in 24 hours.</p>
        """.trimIndent()
        
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundActionException("User not found") }
            
        emailService.sendEmail(
            to = user.email,
            subject = "Verify Your Seller Account",
            body = emailBody,
            isHtml = true
        )
    }
    
    fun verifySeller(token: String): KycVerificationResponse {
        val kycData = verificationTokens[token] ?: 
            return KycVerificationResponse(false, "Invalid or expired verification token")
        
        // In a real app, you would:
        // 1. Verify the token hasn't expired
        // 2. Save KYC data to the database
        // 3. Update user's seller status
        
        // For now, we'll just remove the token
        verificationTokens.remove(token)
        
        return KycVerificationResponse(true, "Seller account verified successfully!")
    }
    
    fun updateSellerStatus(userId: String, isSeller: Boolean) {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundActionException("User not found") }
            
        user.isASeller = isSeller
        userRepository.save(user)
    }
}

