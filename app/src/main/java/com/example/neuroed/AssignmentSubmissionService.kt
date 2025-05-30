package com.example.neuroed

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AssignmentSubmissionService(
    private val context: Context,
    private val apiService: ApiService
) {
    suspend fun submitAssignment(
        assignmentId: Int,
        userId: Int,
        fileUri: Uri?,
        answerText: String
    ): Result<Boolean> {
        return try {
            // Convert parameters to RequestBody objects
            val assignmentIdPart = assignmentId.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            val userIdPart = userId.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            val answerTextPart = answerText
                .toRequestBody("text/plain".toMediaTypeOrNull())

            // Handle file if provided
            val filePart = fileUri?.let { uri ->
                val file = uriToFile(uri)
                val mimeType = getMimeType(uri) ?: "application/octet-stream"
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())

                MultipartBody.Part.createFormData(
                    "submission_file",
                    file.name,
                    requestFile
                )
            }

            // Execute network request
            val response = ApiHelper.executeWithToken { token ->
                apiService.submitAssignment(
                    assignmentIdPart,
                    userIdPart,
                    answerTextPart,
                    filePart,
                    token
                )
            }

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                val errorMsg = response.body()?.message ?: "Server returned error code: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert Uri to File
     */
    private suspend fun uriToFile(uri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Could not open input stream for URI: $uri")

        // Create a temp file
        val fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}"
        val extension = getFileExtension(uri) ?: ""
        val file = File(context.cacheDir, "$fileName.$extension")

        // Copy content to the file
        FileOutputStream(file).use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }

        file
    }

    /**
     * Get file name from Uri
     */
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex("_display_name")
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    /**
     * Get file extension from Uri
     */
    private fun getFileExtension(uri: Uri): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(uri))
    }

    /**
     * Get MIME type from Uri
     */
    private fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
}