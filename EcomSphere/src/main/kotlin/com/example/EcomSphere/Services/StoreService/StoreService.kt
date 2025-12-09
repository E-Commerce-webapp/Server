package com.example.EcomSphere.Services.StoreService

import com.example.EcomSphere.Helper.ForbiddenActionException
import com.example.EcomSphere.Helper.NotFoundActionException
import com.example.EcomSphere.Services.UserService.UserRepository
import org.springframework.stereotype.Service

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val userRepository: UserRepository
) {
    private fun Store.toResponse(): StoreResponse =
        StoreResponse(
            id = this.id!!,
            name = this.name,
            description = this.description,
            address = this.address!!,
            owner = this.owner,
            phoneNumber = this.phoneNumber
        )
    fun createStore(req: CreateStoreRequest, owner: String): StoreResponse {
        val user = userRepository.findById(owner)
            .orElseThrow { NotFoundActionException("User with id ${owner} is not available") }

        if (user.isASeller != true || user.emailConfirm != true) {
            throw ForbiddenActionException("User is not allowed to create a store")
        }

        val store = Store(
            name = req.name,
            description = req.description,
            phoneNumber = req.phoneNumber,
            owner = owner,
            address = req.address
        )

        val saved = storeRepository.save(store)
        return saved.toResponse()
    }

    fun updateStore(req: UpdateStoreRequest, owner: String, id: String): StoreResponse {
        val existing = storeRepository.findById(id)
            .orElseThrow{NotFoundActionException("Store with id ${id} not found")}
        if (existing.owner != owner){
            throw ForbiddenActionException("You are now allow to update store details")
        }else{
            val updated = existing.copy(
                name = req.name ?: existing.name,
                description = req.description ?: existing.description,
                address = req.address ?: existing.address,
                phoneNumber = req.phoneNumber ?: existing.phoneNumber
            )
            val saved = storeRepository.save(updated)
            return saved.toResponse()
        }
    }

    fun deleteStore(id: String, owner: String){
        val store = storeRepository.findById(id)
            .orElseThrow{NotFoundActionException("Store with id ${id} not found")}
        if (store.owner != owner){
            throw ForbiddenActionException("You are not allow to delete this store")
        }else{
            storeRepository.deleteById(id)
        }
    }

    fun getStores(): List<StoreResponse>{
        val stores = storeRepository.findAll()
        return stores.map { store ->
            StoreResponse(
                id = store.id!!,
                name = store.name,
                description = store.description,
                owner = store.owner,
                phoneNumber = store.phoneNumber,
                address = store.address!!
            )
        }
    }

    fun getStoreDetails(id: String): StoreResponse{
        val store = storeRepository.findById(id)
            .orElseThrow{NotFoundActionException("Store with id ${id} not found")}
        return store.toResponse()
    }

}