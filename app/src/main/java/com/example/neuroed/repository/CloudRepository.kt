package com.example.neuroed.repository

import android.content.Context
import android.net.Uri
import com.example.neuroed.model.*
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import com.example.neuroed.utils.StorageFilterOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Repository class for handling cloud storage operations
 * Acts as a single source of truth for cloud storage data
 */
class CloudRepository(private val apiService: ApiService) {

    /**
     * Get all files and folders for a user with optional filtering
     */
    fun getFiles(
        userId: Int,
        folderId: String? = null,
        filter: StorageFilterOption = StorageFilterOption.ALL,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<Result<List<CloudItem>>> = flow {
        try {
            val files = ApiHelper.executeWithToken { token ->
                apiService.getCloudFiles(
                    userId = userId,
                    folderId = folderId,
                    filter = filter.name.lowercase(),
                    page = page,
                    pageSize = pageSize,
                    authHeader = token
                )
            }
            emit(Result.success(files))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Search files by name
     */
    fun searchFiles(userId: Int, query: String): Flow<Result<List<CloudItem>>> = flow {
        try {
            val files = ApiHelper.executeWithToken { token ->
                apiService.searchCloudFiles(
                    userId = userId,
                    query = query,
                    authHeader = token
                )
            }
            emit(Result.success(files))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get recent activity files
     */
    fun getRecentActivity(userId: Int, limit: Int = 10): Flow<Result<List<CloudItem>>> = flow {
        try {
            val files = ApiHelper.executeWithToken { token ->
                apiService.getRecentCloudActivity(
                    userId = userId,
                    limit = limit,
                    authHeader = token
                )
            }
            emit(Result.success(files))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get storage summary for a user
     */
    fun getStorageSummary(userId: Int): Flow<Result<StorageSummary>> = flow {
        try {
            val summary = ApiHelper.executeWithToken { token ->
                apiService.getStorageSummary(
                    userId = userId,
                    authHeader = token
                )
            }
            emit(Result.success(summary))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Upload a file to cloud storage
     */
    fun uploadFile(
        context: Context,
        uri: Uri,
        userId: Int,
        folderId: String? = null
    ): Flow<Result<UploadResponse>> = flow {
        try {
            // Get file from URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = getFileName(context, uri)

            // Create temporary file
            val tempFile = File.createTempFile("upload", fileName, context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            // Create request body
            val requestFile = tempFile.asRequestBody("*/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestFile)
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val folderIdBody = folderId?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = ApiHelper.executeWithToken { token ->
                apiService.uploadFile(
                    file = filePart,
                    userId = userIdBody,
                    folderId = folderIdBody,
                    authHeader = token
                )
            }

            // Clean up temp file
            tempFile.delete()

            emit(Result.success(response))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Create a new folder - CORRECTED
     */
    fun createFolder(
        userId: Int,
        folderName: String,
        parentFolderId: String? = null
    ): Flow<Result<CreateFolderResponse>> = flow {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.createFolder(
                    userId = userId,
                    folderName = folderName,
                    parentFolderId = parentFolderId,
                    authHeader = token
                )
            }
            emit(Result.success(response))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Download a file - CORRECTED with userId
     */
    fun downloadFile(
        context: Context,
        fileId: String,
        fileName: String,
        userId: Int
    ): Flow<Result<Boolean>> = flow {
        try {
            val responseBody = ApiHelper.executeWithToken { token ->
                apiService.downloadFile(
                    fileId = fileId,
                    userId = userId,
                    authHeader = token
                )
            }

            // Save file to downloads directory
            val downloadsDir = context.getExternalFilesDir("Downloads")
            val file = File(downloadsDir, fileName)

            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            emit(Result.success(true))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Delete a file or folder - CORRECTED with userId
     */
    fun deleteFile(fileId: String, userId: Int): Flow<Result<DeleteResponse>> = flow {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.deleteFile(
                    fileId = fileId,
                    userId = userId,
                    authHeader = token
                )
            }
            emit(Result.success(response))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Rename a file or folder - CORRECTED with userId
     */
    fun renameFile(fileId: String, newName: String, userId: Int): Flow<Result<RenameResponse>> = flow {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.renameFile(
                    fileId = fileId,
                    userId = userId,
                    newName = newName,
                    authHeader = token
                )
            }
            emit(Result.success(response))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Share a file - CORRECTED with userId
     */
    fun shareFile(fileId: String, userId: Int): Flow<Result<ShareResponse>> = flow {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.shareFile(
                    fileId = fileId,
                    userId = userId,
                    authHeader = token
                )
            }
            emit(Result.success(response))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Helper function to get file name from URI
     */
    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = "unknown"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}