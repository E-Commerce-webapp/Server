package com.example.EcomSphere.Services.UserService

import com.example.EcomSphere.Helper.CustomUserPrincipal
import com.example.EcomSphere.Helper.NotFoundActionException
import com.example.EcomSphere.Services.StoreService.CreateStoreRequest
import com.example.EcomSphere.Services.StoreService.Store
import com.example.EcomSphere.Services.StoreService.StoreRepository
import com.example.EcomSphere.Services.StoreService.StoreStatus
import com.example.EcomSphere.MiddleWare.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository
){
    @PostMapping("/become-seller")
    fun becomeSellerHandle(@RequestBody req: CreateStoreRequest, request: HttpServletRequest): ResponseEntity<String> {
        val authHeader = request.getHeader("Authorization")
            ?: return ResponseEntity.status(401).body("Missing Authorization header")

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid Authorization header")
        }

        val email = jwtUtil.verifyAndGetEmail(token)
            ?: return ResponseEntity.status(401).body("Invalid or expired token")

        val user = userRepository.findByEmail(email)
            .orElseThrow { NotFoundActionException("User not found") }

        val store = Store(
            name = req.name,
            description = req.description,
            address = req.address,
            owner = user.id!!,
            phoneNumber = req.phoneNumber,
            status = StoreStatus.PENDING
        )
        storeRepository.save(store)

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
