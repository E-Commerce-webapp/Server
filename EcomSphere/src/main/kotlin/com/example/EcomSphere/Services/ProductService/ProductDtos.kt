package com.example.EcomSphere.Services.ProductService

data class CreateProductRequest(val title: String, val description: String, val category: String, val price: Float, val stock: Int, val images: String, val sellerId: String?)
data class UpdateProductRequest(val title: String?, val description: String?, val category: String?, val price: Float?, val stock: Int?, val images: String? )
data class DeleteProductRequest(val id: String)
data class GetProductRequest(val id: String)
data class GetAllProductsResponse(val id: String, val title: String, val category: String, val description: String, val price: Float, val stock: Int, val images: List<String>)
data class ProductsApiResponse(
    val products: List<GetAllProductsResponse>,
    val total: Int,
    val skip: Int,
    val limit: Int
)
data class ProductResponse(
    val id: String,
    val title: String,
    val description: String,
    val price: Float,
    val stock: Int,
    val images: List<String>,
    val sellerId: String,
    val category: String
)
