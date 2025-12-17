package com.example.EcomSphere.Services.UserService

import com.example.EcomSphere.Helper.CustomUserPrincipal
import com.example.EcomSphere.Helper.ForbiddenActionException
import com.example.EcomSphere.Helper.NotFoundActionException
import com.example.EcomSphere.Services.StoreService.CreateStoreRequest
import com.example.EcomSphere.Services.StoreService.Store
import com.example.EcomSphere.Services.StoreService.StoreRepository
import com.example.EcomSphere.Services.StoreService.StoreStatus
import com.example.EcomSphere.MiddleWare.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository
){
    @PostMapping("/become-seller")
    fun becomeSellerHandle(
        @RequestBody req: CreateStoreRequest,
        request: HttpServletRequest
    ): ResponseEntity<String> {

        println(">>> becomeSellerHandle called with body: $req")

        val authHeader = request.getHeader("Authorization")
            ?: return ResponseEntity.status(401).body("Missing Authorization header")
        println(">>> Authorization header: $authHeader")

        val token = authHeader.removePrefix("Bearer ").trim()
        println(">>> Raw token: $token")

        val email = jwtUtil.verifyAndGetEmail(token)
        println(">>> Email from token: $email")

        if (email == null) {
            return ResponseEntity.status(401).body("Invalid or expired token")
        }

        val user = userRepository.findByEmail(email)
            .orElseThrow { NotFoundActionException("User not found") }

        println(">>> User found: id=${user.id}, email=${user.email}")

        // Check if user already has a store (each seller can only have one store)
        val existingStores = storeRepository.findByOwner(user.id!!)
        if (existingStores.isNotEmpty()) {
            throw ForbiddenActionException("You already have a store. Each seller can only have one store.")
        }

        val store = Store(
            name = req.name,
            description = req.description,
            address = req.address,
            owner = user.id!!,
            phoneNumber = req.phoneNumber,
            status = StoreStatus.PENDING
        )
        storeRepository.save(store)

        println(">>> Store saved for user ${user.id}")

        emailService.sendVerificationEmail(email, token)
        println(">>> Verification email sent")

        return ResponseEntity.ok("Verification email sent.")
    }

    @GetMapping("")
    fun findUserByEmail(authentication: Authentication): ResponseEntity<GetUsersResponse>{
        val principal = authentication.principal as CustomUserPrincipal
        val id = principal.id
        val user = userService.findUserById(id)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/checkout-info")
    fun saveCheckoutInfo(
        @RequestBody request: SaveCheckoutInfoRequest,
        authentication: Authentication
    ): ResponseEntity<GetUsersResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val userId = principal.id
        val updatedUser = userService.saveCheckoutInfo(userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @PostMapping("/resend-verification")
    fun resendVerificationEmail(request: HttpServletRequest): ResponseEntity<String> {
        val authHeader = request.getHeader("Authorization")
            ?: return ResponseEntity.status(401).body("Missing Authorization header")

        val token = authHeader.removePrefix("Bearer ").trim()
        val email = jwtUtil.verifyAndGetEmail(token)
            ?: return ResponseEntity.status(401).body("Invalid or expired token")

        val user = userRepository.findByEmail(email)
            .orElseThrow { NotFoundActionException("User not found") }

        // Check if user has a pending store
        val stores = storeRepository.findByOwner(user.id!!)
        val pendingStore = stores.firstOrNull { it.status == StoreStatus.PENDING }
            ?: throw ForbiddenActionException("No pending store found. You may have already verified your email.")

        // Generate a new token and send verification email
        val newToken = jwtUtil.generate(email)
        
        // Send email directly without the isASeller check
        try {
            val verifyUrl = "http://localhost:8080/auth/verify-email/$newToken"
            val message = org.springframework.mail.SimpleMailMessage().apply {
                setTo(email)
                subject = "Verify your email - Resend"
                text = """
                    Hi,
                    
                    Please verify your email by clicking the link below:
                    $verifyUrl
                    
                    If you did not request this, please ignore this email.
                """.trimIndent()
            }
            emailService.sendEmail(message)
            println(">>> Verification email resent to $email")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return ResponseEntity.ok("Verification email resent successfully.")
    }
}
