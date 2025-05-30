package com.example.neuroed.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Cloud item data model for handling files and folders from cloud storage
 */
data class CloudItem(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("size")
    val size: String,

    @SerializedName("size_bytes")
    val sizeBytes: Long = 0L,

    @SerializedName("last_modified")
    val lastModifiedString: String,

    @SerializedName("is_folder")
    val isFolder: Boolean = false,

    @SerializedName("parent_folder")
    val parentFolder: String? = null,

    @SerializedName("download_url")
    val downloadUrl: String? = null,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerializedName("shared")
    val isShared: Boolean = false,

    @SerializedName("owner")
    val owner: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
) {
    /**
     * Convert string date to Date object
     */
    val lastModified: Date
        get() = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(lastModifiedString)
                ?: Date()
        } catch (e: Exception) {
            Date()
        }

    /**
     * Get file extension
     */
    fun getFileExtension(): String {
        return if (isFolder) "" else name.substringAfterLast(".", "")
    }

    /**
     * Check if file is an image
     */
    fun isImage(): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        return imageExtensions.contains(getFileExtension().lowercase())
    }

    /**
     * Check if file is a video
     */
    fun isVideo(): Boolean {
        val videoExtensions = listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm")
        return videoExtensions.contains(getFileExtension().lowercase())
    }

    /**
     * Check if file is audio
     */
    fun isAudio(): Boolean {
        val audioExtensions = listOf("mp3", "wav", "flac", "aac", "ogg", "wma")
        return audioExtensions.contains(getFileExtension().lowercase())
    }

    /**
     * Check if file is a document
     */
    fun isDocument(): Boolean {
        val docExtensions = listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
        return docExtensions.contains(getFileExtension().lowercase())
    }

    /**
     * Get formatted file size
     */
    fun getFormattedSize(): String {
        if (isFolder) return "Folder"

        val bytes = sizeBytes
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}




data class SharedLink(
    val id: String,
    val shareUrl: String,
    val itemName: String,
    val itemType: String,
    val createdAt: String,
    val expiresAt: String?,
    val isActive: Boolean,
    val accessCount: Int
)

data class BulkOperationResponse(
    val success: Boolean,
    val message: String,
    val successCount: Int,
    val failedCount: Int,
    val failedItems: List<String>?
)