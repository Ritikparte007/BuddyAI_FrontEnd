package com.example.neuroed.model

import com.google.gson.annotations.SerializedName

/**
 * Storage summary data model
 */
data class StorageSummary(
    @SerializedName("total_storage")
    val totalStorage: Long,

    @SerializedName("used_storage")
    val usedStorage: Long,

    @SerializedName("documents_size")
    val documentsSize: Long = 0L,

    @SerializedName("images_size")
    val imagesSize: Long = 0L,

    @SerializedName("videos_size")
    val videosSize: Long = 0L,

    @SerializedName("audio_size")
    val audioSize: Long = 0L,

    @SerializedName("other_size")
    val otherSize: Long = 0L,

    @SerializedName("file_count")
    val fileCount: Int = 0,

    @SerializedName("folder_count")
    val folderCount: Int = 0
) {
    fun getStorageUsedGB(): Double = usedStorage / (1024.0 * 1024.0 * 1024.0)
    fun getStorageTotalGB(): Double = totalStorage / (1024.0 * 1024.0 * 1024.0)
    fun getStoragePercentage(): Float = if (totalStorage > 0) usedStorage.toFloat() / totalStorage.toFloat() else 0f

    fun getDocumentsGB(): Double = documentsSize / (1024.0 * 1024.0 * 1024.0)
    fun getImagesGB(): Double = imagesSize / (1024.0 * 1024.0 * 1024.0)
    fun getVideosGB(): Double = videosSize / (1024.0 * 1024.0 * 1024.0)
    fun getAudioGB(): Double = audioSize / (1024.0 * 1024.0 * 1024.0)
    fun getOtherGB(): Double = otherSize / (1024.0 * 1024.0 * 1024.0)
}