package com.example.neuroed.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.ResponseBody
import java.io.*

/**
 * Utility class for handling file operations
 */
class FileUtils(private val context: Context) {

    data class FileInfo(
        val file: File? = null,
        val name: String = "unknown",
        val size: Long = 0,
        val mimeType: String? = null
    )

    /**
     * Get file information from a Uri
     */
    fun getFileInfoFromUri(uri: Uri): FileInfo {
        val mimeType = context.contentResolver.getType(uri)
        val fileName = getFileNameFromUri(uri)

        // Create a temporary file
        val tempFile = createTempFileFromUri(uri)
        val fileSize = tempFile?.length() ?: 0

        return FileInfo(
            file = tempFile,
            name = fileName,
            size = fileSize,
            mimeType = mimeType
        )
    }

    /**
     * Get file name from a Uri
     */
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "unknown"

        // Try to get the display name from the content resolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor: Cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }

        // If we still don't have a name, try the path segments
        if (fileName == "unknown") {
            val pathSegments = uri.pathSegments
            if (pathSegments.isNotEmpty()) {
                fileName = pathSegments.last()
            }
        }

        return fileName
    }

    /**
     * Create a temporary file from a Uri
     */
    private fun createTempFileFromUri(uri: Uri): File? {
        val name = getFileNameFromUri(uri)
        val extension = name.substringAfterLast('.', "")
        val prefix = name.substringBeforeLast('.', "temp")

        try {
            val tempFile = File.createTempFile(prefix, ".$extension", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Format file size to a human-readable string
     */
    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes bytes"
        }
    }

    /**
     * Save a response body to a file in the downloads directory
     */
    fun saveResponseBodyToFile(body: ResponseBody, fileName: String): File {
        val targetFile = File(context.getExternalFilesDir(null), fileName)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            var fileSizeDownloaded: Long = 0

            inputStream = body.byteStream()
            outputStream = FileOutputStream(targetFile)

            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                fileSizeDownloaded += read
            }
            outputStream.flush()
            return targetFile
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    /**
     * Get a file type from its extension
     */
    fun getFileTypeFromName(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()

        return when (extension) {
            "pdf" -> "PDF"
            "doc", "docx" -> "Word"
            "xls", "xlsx" -> "Excel"
            "ppt", "pptx" -> "PowerPoint"
            "jpg", "jpeg", "png", "gif", "bmp" -> "Image"
            "mp4", "avi", "mov", "mkv" -> "Video"
            "mp3", "wav", "ogg", "m4a" -> "Audio"
            else -> "File"
        }
    }
}