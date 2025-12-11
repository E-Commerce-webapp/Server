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
            address = user.address,
            savedShippingAddress = user.savedShippingAddress,
            savedPaymentMethod = user.savedPaymentMethod
        )
        
        println("Sending response: $response")
        return response
    }

    fun saveCheckoutInfo(userId: String, request: SaveCheckoutInfoRequest): GetUsersResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundActionException("User with ID $userId is not available") }
        
        request.shippingAddress?.let {
            user.savedShippingAddress = SavedShippingAddress(
                fullName = it.fullName,
                addressLine1 = it.addressLine1,
                city = it.city,
                postalCode = it.postalCode,
                country = it.country
            )
        }
        
        request.paymentMethod?.let {
            user.savedPaymentMethod = SavedPaymentMethod(
                cardLastFour = it.cardLastFour,
                cardExpiry = it.cardExpiry,
                cardType = it.cardType
            )
        }
        
        val savedUser = userRepository.save(user)
        
        return GetUsersResponse(
            firstName = savedUser.firstName,
            lastName = savedUser.lastName,
            email = savedUser.email,
            id = savedUser.id,
            isASeller = savedUser.isASeller!!,
            emailConfirm = savedUser.emailConfirm!!,
            address = savedUser.address,
            savedShippingAddress = savedUser.savedShippingAddress,
            savedPaymentMethod = savedUser.savedPaymentMethod
        )
    }
}

