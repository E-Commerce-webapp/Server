package com.example.EcomSphere.Services.ProductService

import com.example.EcomSphere.Config.WebClientConfig
import com.example.EcomSphere.Helper.ForbiddenActionException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
@Service
class ProductService(
    @Value("\${external.api}") private val baseUrl: String,
    private val productRepository: ProductRepository,
    private val webClientConfig: WebClientConfig
){

    private fun Product.toResponse(): ProductResponse =
        ProductResponse(
            id = this.id!!,
            title = this.title,
            description = this.description,
            price = this.price,
            stock = this.stock,
            images = listOf(this.images),
            sellerId = this.sellerId
        )

    fun getAllProductsExternal(): List<GetAllProductsResponse> {
        val response = webClientConfig.webClient().get()
            .uri(baseUrl)
            .retrieve()
            .bodyToMono(ProductsApiResponse::class.java)
            .block()

        return response?.products ?: emptyList()
    }

    fun getAllProductInternal(): List<GetAllProductsResponse>{
        val products = productRepository.findAll()

        return products.map { product ->
            GetAllProductsResponse(
                id = product.id!!,
                title = product.title,
                description = product.description,
                price = product.price,
                stock = product.stock,
                images = listOf(product.images)
            )
        }
    }

    fun addProduct(request: CreateProductRequest, serllerId: String): ProductResponse{
        val product = Product(
            title = request.title,
            description = request.description,
            price = request.price,
            stock = request.stock,
            images = request.images,
            sellerId = serllerId,
            category = request.category
        )
        val saved = productRepository.save(product)
        return saved.toResponse()
    }

    fun updateProduct(id: String, request: UpdateProductRequest, sellerId: String): ProductResponse{
        val existing = productRepository.findById(id)
            .orElseThrow {ForbiddenActionException("Product with id=$id not found")}
        if (existing.sellerId != sellerId){
            throw ForbiddenActionException("You are not allow to edit this product")
        }else{
            val updated = existing.copy(
                title = request.title ?: existing.title,
                description = request.description ?: existing.description,
                price = request.price ?: existing.price,
                stock = request.stock ?: existing.stock,
                images = request.images ?: existing.images
            )

            val saved = productRepository.save(updated)
            return saved.toResponse()
        }
    }

    fun deleteProduct(id: String, sellerId: String) {
        val productRef = productRepository.findById(id)
            .orElseThrow{ForbiddenActionException("Product not found")}
        if (productRef.sellerId != sellerId){
            throw ForbiddenActionException("You are not allow to delete this product")
        }
        productRepository.deleteById(id)
    }
}