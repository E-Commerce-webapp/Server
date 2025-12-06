package com.example.EcomSphere.Services.UserService

import com.example.EcomSphere.Helper.NotFoundActionException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
){
    fun findUserByEmail(email: String): GetUsersResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { NotFoundActionException("User with email $email is not available") }

        return GetUsersResponse(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName
        )
    }

}

