package com.example.EcomSphere.Services.CloudinaryService

import com.cloudinary.Cloudinary
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

data class CloudinaryUploadResult(
    val url: String,
    val secureUrl: String,
    val publicId: String
)

@Service
class CloudinaryService(
    private val cloudinary: Cloudinary
) {
    fun uploadImage(file: MultipartFile, folder: String = "ecomsphere"): CloudinaryUploadResult {
        require(!file.isEmpty) { "File is empty" }

        val uploadResult = cloudinary.uploader().upload(
            file.bytes,
            mapOf(
                "folder" to folder,
                "resource_type" to "image"
            )
        )

        return CloudinaryUploadResult(
            url = uploadResult["url"].toString(),
            secureUrl = uploadResult["secure_url"].toString(),
            publicId = uploadResult["public_id"].toString()
        )
    }

    fun deleteByPublicId(publicId: String): Boolean {
        val result = cloudinary.uploader().destroy(
            publicId,
            mapOf("resource_type" to "image")
        )
        return result["result"]?.toString() == "ok"
    }
}
