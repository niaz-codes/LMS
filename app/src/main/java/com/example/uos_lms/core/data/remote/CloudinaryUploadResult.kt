package com.example.uos_lms.core.data.remote

/** Everything worth persisting about a file that now lives in Cloudinary. */
data class CloudinaryUploadResult(
    val url: String,
    val publicId: String,
    val fileName: String,
    val resourceType: String,
    val bytes: Long,
)
