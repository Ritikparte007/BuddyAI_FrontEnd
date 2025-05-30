package com.example.neuroed.model

import com.google.gson.annotations.SerializedName

/**
 * Upload response model
 */
data class UploadResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("file")
    val file: CloudItem? = null
)

/**
 * Folder creation response
 */
data class CreateFolderResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("folder")
    val folder: CloudItem? = null
)

/**
 * Share response model
 */
data class ShareResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("share_url")
    val shareUrl: String,

    @SerializedName("message")
    val message: String
)

/**
 * Delete response model
 */
data class DeleteResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

/**
 * Rename response model
 */
data class RenameResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("item")
    val item: CloudItem? = null
)