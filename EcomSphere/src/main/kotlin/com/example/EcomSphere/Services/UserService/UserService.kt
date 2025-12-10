package com.example.EcomSphere.Services.UserService

import com.example.EcomSphere.Helper.NotFoundActionException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
){
    fun findUserById(id: String): GetUsersResponse {
        val user = userRepository.findById(id)
            .orElseThrow { NotFoundActionException("User with ID $id is not available") }
        
        println("User data from DB: $user")
        
        val response = GetUsersResponse(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            id = user.id,
            isASeller = user.isASeller!!,
            emailConfirm = user.emailConfirm!!,
            address = user.address
        )
        
        println("Sending response: $response")
        return response
    }

}

