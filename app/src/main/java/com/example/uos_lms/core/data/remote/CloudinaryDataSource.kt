package com.example.uos_lms.core.data.remote

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.example.uos_lms.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to Cloudinary's Upload API directly over HTTPS (signed requests) instead of
 * using the Cloudinary Android SDK, so the exact signing/multipart behavior is fully
 * under our control and auditable. Replaces the old Firebase Storage-backed
 * StorageDataSource — same 4 upload flows (profile photo, assignment attachment,
 * assignment submission, study material) plus a generic signed delete.
 */
@Singleton
class CloudinaryDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {
    private val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val apiKey = BuildConfig.CLOUDINARY_API_KEY
    private val apiSecret = BuildConfig.CLOUDINARY_API_SECRET

    suspend fun uploadProfilePhoto(
        uid: String,
        uri: Uri,
        onProgress: (Int) -> Unit = {},
    ): CloudinaryUploadResult {
        validateImage(uri)
        return upload(
            uri = uri,
            publicId = "profile_photos/$uid",
            resourceType = "image",
            fileName = "profile.jpg",
            onProgress = onProgress,
        )
    }

    /** Best-effort — a missing/already-deleted asset is not treated as an error. */
    suspend fun deleteProfilePhoto(uid: String) {
        destroy(publicId = "profile_photos/$uid", resourceType = "image")
    }

    suspend fun uploadAssignmentFile(
        subjectId: String,
        assignmentId: String,
        uri: Uri,
        onProgress: (Int) -> Unit = {},
    ): CloudinaryUploadResult = uploadDocument(
        folder = "assignments/$subjectId/$assignmentId",
        uri = uri,
        onProgress = onProgress,
    )

    suspend fun uploadSubmissionFile(
        assignmentId: String,
        studentUid: String,
        uri: Uri,
        onProgress: (Int) -> Unit = {},
    ): CloudinaryUploadResult = uploadDocument(
        folder = "assignment_submissions/$assignmentId/$studentUid",
        uri = uri,
        onProgress = onProgress,
    )

    suspend fun uploadMaterialFile(
        subjectId: String,
        materialId: String,
        uri: Uri,
        onProgress: (Int) -> Unit = {},
    ): CloudinaryUploadResult = uploadDocument(
        folder = "materials/$subjectId/$materialId",
        uri = uri,
        onProgress = onProgress,
    )

    /** Best-effort delete of any previously-uploaded file, by its stored public ID. */
    suspend fun delete(publicId: String, resourceType: String) {
        destroy(publicId, resourceType)
    }

    private suspend fun uploadDocument(
        folder: String,
        uri: Uri,
        onProgress: (Int) -> Unit,
    ): CloudinaryUploadResult {
        validateDocument(uri)
        val resourceType = resolveResourceType(uri)
        val fileName = resolveFileName(uri)
        val publicId = buildPublicId(folder, fileName, resourceType)
        return upload(uri, publicId, resourceType, fileName, onProgress)
    }

    private suspend fun upload(
        uri: Uri,
        publicId: String,
        resourceType: String,
        fileName: String,
        onProgress: (Int) -> Unit,
    ): CloudinaryUploadResult = withContext(Dispatchers.IO) {
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val paramsToSign = mapOf(
            "invalidate" to "true",
            "overwrite" to "true",
            "public_id" to publicId,
            "timestamp" to timestamp,
        )
        val signature = sign(paramsToSign, apiSecret)

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not read the selected file.")
        val mimeType = mimeType(uri)?.toMediaTypeOrNull()

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("public_id", publicId)
            .addFormDataPart("timestamp", timestamp)
            .addFormDataPart("overwrite", "true")
            .addFormDataPart("invalidate", "true")
            .addFormDataPart("api_key", apiKey)
            .addFormDataPart("signature", signature)
            .addFormDataPart("file", fileName, ProgressRequestBody(bytes, mimeType, onProgress))
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload")
            .post(multipart)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error(cloudinaryErrorMessage(bodyString, response.code))
            }
            val json = JSONObject(bodyString)
            CloudinaryUploadResult(
                url = json.getString("secure_url"),
                publicId = json.getString("public_id"),
                fileName = fileName,
                resourceType = resourceType,
                bytes = json.optLong("bytes", bytes.size.toLong()),
            )
        }
    }

    private suspend fun destroy(publicId: String, resourceType: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val paramsToSign = mapOf(
                    "invalidate" to "true",
                    "public_id" to publicId,
                    "timestamp" to timestamp,
                )
                val signature = sign(paramsToSign, apiSecret)
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("public_id", publicId)
                    .addFormDataPart("timestamp", timestamp)
                    .addFormDataPart("invalidate", "true")
                    .addFormDataPart("api_key", apiKey)
                    .addFormDataPart("signature", signature)
                    .build()
                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/$resourceType/destroy")
                    .post(body)
                    .build()
                okHttpClient.newCall(request).execute().use { /* best-effort cleanup */ }
            }
        }
    }

    private fun cloudinaryErrorMessage(body: String, httpCode: Int): String =
        runCatching { JSONObject(body).getJSONObject("error").getString("message") }
            .getOrNull()
            ?: "Upload failed (HTTP $httpCode)."

    private fun resolveResourceType(uri: Uri): String {
        val mime = mimeType(uri).orEmpty()
        return when {
            mime.startsWith("image/") -> "image"
            mime.startsWith("video/") -> "video"
            else -> "raw"
        }
    }

    private fun resolveFileName(uri: Uri): String {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)?.let { return it }
                }
            }
        }
        return uri.lastPathSegment ?: "file"
    }

    private fun buildPublicId(folder: String, fileName: String, resourceType: String): String {
        val sanitized = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        // Raw resources treat the file extension as part of the public ID (Cloudinary
        // doesn't auto-append a format for them like it does for image/video), so it
        // must be kept in; image/video would otherwise end up with a doubled extension.
        return if (resourceType == "raw") {
            "$folder/$sanitized"
        } else {
            "$folder/${sanitized.substringBeforeLast('.', sanitized)}"
        }
    }

    private fun querySize(uri: Uri): Long? {
        if (uri.scheme == "file") return uri.path?.let(::File)?.takeIf { it.exists() }?.length()
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                return cursor.getLong(sizeIndex)
            }
        }
        return null
    }

    private fun validateImage(uri: Uri) {
        val mime = mimeType(uri)
        require(mime in ALLOWED_IMAGE_MIME_TYPES) { "Only JPG, PNG, or WEBP images are allowed." }
        val size = querySize(uri)
        require(size == null || size <= MAX_IMAGE_BYTES) { "Image must be smaller than 5 MB." }
    }

    private fun validateDocument(uri: Uri) {
        val mime = mimeType(uri)
        require(mime in ALLOWED_DOCUMENT_MIME_TYPES) { "This file type isn't supported." }
        val size = querySize(uri)
        require(size == null || size <= MAX_DOCUMENT_BYTES) { "File must be smaller than 25 MB." }
    }

    private fun mimeType(uri: Uri): String? =
        context.contentResolver.getType(uri)
            ?: uri.path?.substringAfterLast('.', "")
                ?.lowercase()
                ?.let(MimeTypeMap.getSingleton()::getMimeTypeFromExtension)

    private companion object {
        const val MAX_IMAGE_BYTES = 5L * 1024 * 1024
        const val MAX_DOCUMENT_BYTES = 25L * 1024 * 1024

        val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")
        val ALLOWED_DOCUMENT_MIME_TYPES = setOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip",
            "video/mp4",
            "image/jpeg",
            "image/png",
            "image/webp",
        )
    }
}

/**
 * Generates a Cloudinary API request signature: params sorted alphabetically by key,
 * joined as "key=value" with "&", the API secret appended with no delimiter, then
 * SHA-1 hex digest — Cloudinary's documented signing algorithm.
 */
private fun sign(params: Map<String, String>, apiSecret: String): String {
    val toSign = params.toSortedMap().entries.joinToString("&") { "${it.key}=${it.value}" } + apiSecret
    val digest = MessageDigest.getInstance("SHA-1").digest(toSign.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

/** Streams [bytes] to OkHttp in chunks so [onProgress] (0-100) reflects actual upload progress. */
private class ProgressRequestBody(
    private val bytes: ByteArray,
    private val mediaType: okhttp3.MediaType?,
    private val onProgress: (Int) -> Unit,
) : RequestBody() {
    override fun contentType() = mediaType
    override fun contentLength() = bytes.size.toLong()

    override fun writeTo(sink: BufferedSink) {
        val source = Buffer().write(bytes)
        val total = bytes.size.toLong()
        var written = 0L
        val chunk = Buffer()
        var read: Long
        while (source.read(chunk, CHUNK_SIZE).also { read = it } != -1L) {
            sink.write(chunk, read)
            written += read
            if (total > 0) onProgress(((written * 100) / total).toInt())
        }
    }

    private companion object {
        const val CHUNK_SIZE = 8192L
    }
}
