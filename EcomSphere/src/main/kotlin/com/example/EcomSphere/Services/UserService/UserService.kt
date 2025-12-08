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

        return GetUsersResponse(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isASeller = user.isASeller!!,
            emailConfirm = user.emailConfirm!!
        )
    }

}

