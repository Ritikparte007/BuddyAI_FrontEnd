package com.example.neuroed.utils

import com.example.neuroed.model.CloudItem


/**
 * Enum for storage filter options
 */
enum class StorageFilterOption {
    ALL,
    DOCUMENTS,
    IMAGES,
    VIDEOS,
    AUDIO
}

/**
 * Sealed class for cloud file operations
 */
sealed class CloudFileOperation {
    data class View(val item: CloudItem) : CloudFileOperation()
    data class Download(val item: CloudItem) : CloudFileOperation()
    data class Share(val item: CloudItem) : CloudFileOperation()
    data class Rename(val item: CloudItem) : CloudFileOperation()
    data class Delete(val item: CloudItem) : CloudFileOperation()
    class Upload : CloudFileOperation()
    class CreateFolder : CloudFileOperation()
}

/**
 * Resource wrapper class for handling API responses
 */
sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T?) : Resource<T>()
    data class Error<T>(val message: String, val exception: Throwable? = null) : Resource<T>()
}