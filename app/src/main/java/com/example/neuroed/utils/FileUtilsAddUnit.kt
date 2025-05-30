package com.example.neuroed.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for file operations in AddUnit screen
 */
object FileUtilsAddunit {

    /**
     * Get the display name of a file from its URI
     */
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
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
        return result ?: "unknown_file"
    }

    /**
     * Copy a file from URI to a local file
     */
    fun copyUriToFile(context: Context, uri: Uri, destFile: File) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /**
     * Get the MIME type of a file from its URI
     */
    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }

    /**
     * Create a temporary file from URI
     */
    fun createTempFileFromUri(context: Context, uri: Uri, prefix: String, suffix: String): File {
        val tempFile = File.createTempFile(prefix, suffix, context.cacheDir)
        copyUriToFile(context, uri, tempFile)
        return tempFile
    }

    /**
     * Get file size from URI in KB
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && it.moveToFirst()) {
                it.getLong(sizeIndex) / 1024 // Convert to KB
            } else {
                -1
            }
        } ?: -1
    }

    /**
     * Check if file is an image based on MIME type
     */
    fun isImageFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType.startsWith("image/")
    }

    /**
     * Check if file is a PDF based on MIME type
     */
    fun isPdfFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType == "application/pdf"
    }

    /**
     * Check if file is a document (DOC, DOCX, TXT)
     */
    fun isDocumentFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType == "application/msword" ||
                mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                mimeType == "text/plain"
    }
}