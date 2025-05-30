package com.example.neuroed.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.InputMode
import com.example.neuroed.network.ApiService
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.utils.FileUtilsAddunit
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ViewModel responsible for handling LLM-based text extraction from various sources
 * such as camera images, PDF files, and other document types.
 */
class LlmExtractViewModel : ViewModel() {

    // LiveData to hold the extracted text
    private val _extractedText = MutableLiveData<String>()
    val extractedText: LiveData<String> = _extractedText

    // LiveData for error handling
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Main function to extract text from a file based on the input mode
     */
    fun extractTextFromFile(
        fileUri: Uri?,
        prompt: String,
        inputMode: InputMode,
        context: Context
    ) {
        if (fileUri == null) {
            _errorMessage.value = "No file selected"
            return
        }

        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Process differently based on input mode
                when (inputMode) {
                    InputMode.CAMERA -> {
                        processImageWithOcr(fileUri, prompt, context)
                    }
                    InputMode.PDF -> {
                        processPdfWithLlm(fileUri, prompt, context)
                    }
                    InputMode.OTHER_FILE -> {
                        processDocumentWithLlm(fileUri, prompt, context)
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            _errorMessage.value = "Unsupported input mode"
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LlmExtractViewModel", "Error extracting text: ${e.message}")
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Process camera images with OCR first, then send to LLM
     */
    private suspend fun processImageWithOcr(fileUri: Uri, prompt: String, context: Context) {
        try {
            // Use ML Kit for OCR to extract text from image
            val extractedText = performOcrOnImage(fileUri, context)

            // If OCR extracted text, send it to LLM for processing
            if (extractedText.isNotEmpty()) {
                processWithLlm(extractedText, prompt)
            } else {
                // If local OCR failed, try server-side processing
                processImageWithServer(fileUri, prompt, context)
            }
        } catch (e: Exception) {
            Log.e("LlmExtractViewModel", "OCR processing failed: ${e.message}")
            // Fall back to server-side processing if local OCR fails
            processImageWithServer(fileUri, prompt, context)
        }
    }

    /**
     * Perform OCR on an image using ML Kit
     */
    private suspend fun performOcrOnImage(fileUri: Uri, context: Context): String = suspendCancellableCoroutine { continuation ->
        try {
            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromFilePath(context, fileUri)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    textRecognizer.close()
                    continuation.resume(extractedText)
                }
                .addOnFailureListener { e ->
                    textRecognizer.close()
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Process image with server if local OCR fails
     */
    private suspend fun processImageWithServer(fileUri: Uri, prompt: String, context: Context) {
        try {
            // Get the API service with authentication
            RetrofitClient.getApiWithToken { apiService ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        // Create a temp file from the URI
                        val tempFile = FileUtilsAddunit.createTempFileFromUri(
                            context,
                            fileUri,
                            "image_",
                            ".jpg"
                        )

                        // Create multipart request
                        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
                        val promptBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())

                        // Call the API
                        val response = apiService.extractTextFromImage(imagePart, promptBody)

                        // Handle response
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string() ?: ""
                            val jsonObject = JSONObject(responseBody)
                            val processedText = jsonObject.optString("processed_text", "")

                            withContext(Dispatchers.Main) {
                                _extractedText.value = processedText
                                _isLoading.value = false
                            }
                        } else {
                            throw Exception("Server error: ${response.code()} ${response.message()}")
                        }

                        // Clean up temp file
                        tempFile.delete()
                    } catch (e: Exception) {
                        handleApiError("Server image processing failed", e)
                    }
                }
            }
        } catch (e: Exception) {
            handleApiError("Failed to get API service", e)
        }
    }

    /**
     * Process PDF files
     */
    private suspend fun processPdfWithLlm(fileUri: Uri, prompt: String, context: Context) {
        try {
            // Check if PDF is too large (over 10MB)
            val fileSizeKB = FileUtilsAddunit.getFileSize(context, fileUri)
            if (fileSizeKB > 10 * 1024) { // Greater than 10MB
                // Large PDF - use server-side processing only
                processLargePdfWithServer(fileUri, prompt, context)
                return
            }

            // Create a temporary file to store PDF content
            val tempFile = FileUtilsAddunit.createTempFileFromUri(
                context,
                fileUri,
                "temp_pdf_",
                ".pdf"
            )

            // Extract text from PDF
            val pdfText = StringBuilder()

            try {
                // Use PdfRenderer to render PDF pages as bitmaps
                val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)

                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                // Process each page of the PDF (limit to first 8 pages for performance)
                val pageCount = minOf(pdfRenderer.pageCount, 8)
                for (i in 0 until pageCount) {
                    try {
                        val page = pdfRenderer.openPage(i)

                        // Render the page to a bitmap with higher resolution for better OCR
                        val bitmap = Bitmap.createBitmap(
                            page.width * 2,
                            page.height * 2,
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        // Use OCR to extract text from the rendered page
                        val image = InputImage.fromBitmap(bitmap, 0)
                        val result = suspendCancellableCoroutine<String> { continuation ->
                            textRecognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    continuation.resume(visionText.text)
                                }
                                .addOnFailureListener { e ->
                                    continuation.resumeWithException(e)
                                }
                        }

                        pdfText.append(result)
                        pdfText.append("\n\n--- Page ${i+1} ---\n\n")

                        page.close()
                        bitmap.recycle()
                    } catch (e: Exception) {
                        Log.e("LlmExtractViewModel", "Error processing PDF page ${i+1}: ${e.message}")
                        pdfText.append("\n\n--- Error on Page ${i+1} ---\n\n")
                    }
                }

                pdfRenderer.close()
                fileDescriptor.close()
                textRecognizer.close()
            } catch (e: Exception) {
                Log.e("LlmExtractViewModel", "Error rendering PDF: ${e.message}")
                // If rendering fails, try server-side processing
                processLargePdfWithServer(fileUri, prompt, context)
                tempFile.delete()
                return
            }

            // If text was extracted, send to LLM for processing
            if (pdfText.isNotEmpty()) {
                processWithLlm(pdfText.toString(), prompt)
            } else {
                // If no text extracted, try server-side processing
                processLargePdfWithServer(fileUri, prompt, context)
            }

            // Clean up temporary file
            tempFile.delete()

        } catch (e: Exception) {
            Log.e("LlmExtractViewModel", "PDF processing failed: ${e.message}")
            // Try server-side processing as fallback
            processLargePdfWithServer(fileUri, prompt, context)
        }
    }

    /**
     * Process large PDFs with server-side processing
     */
    private suspend fun processLargePdfWithServer(fileUri: Uri, prompt: String, context: Context) {
        try {
            // Get the API with authentication
            RetrofitClient.getApiWithToken { apiService ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        // Create a temp file from the URI if needed
                        val tempFile = FileUtilsAddunit.createTempFileFromUri(
                            context,
                            fileUri,
                            "pdf_",
                            ".pdf"
                        )

                        // Create multipart request
                        val requestFile = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
                        val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                        val promptBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())

                        // Call the API
                        val response = apiService.extractTextFromDocument(filePart, promptBody)

                        // Handle response
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string() ?: ""
                            val jsonObject = JSONObject(responseBody)
                            val processedText = jsonObject.optString("processed_text", "")

                            withContext(Dispatchers.Main) {
                                _extractedText.value = processedText
                                _isLoading.value = false
                            }
                        } else {
                            throw Exception("Server error: ${response.code()} ${response.message()}")
                        }

                        // Clean up temp file
                        tempFile.delete()
                    } catch (e: Exception) {
                        handleApiError("Server PDF processing failed", e)
                    }
                }
            }
        } catch (e: Exception) {
            handleApiError("Failed to get API service", e)
        }
    }

    /**
     * Process other document types (DOC, DOCX, TXT)
     */
    private suspend fun processDocumentWithLlm(fileUri: Uri, prompt: String, context: Context) {
        try {
            // Get MIME type
            val mimeType = FileUtilsAddunit.getMimeType(context, fileUri)

            // For text files, we can read directly without server
            if (mimeType == "text/plain") {
                try {
                    // Try to read text file directly
                    val text = context.contentResolver.openInputStream(fileUri)?.use { input ->
                        input.bufferedReader().use { it.readText() }
                    } ?: ""

                    if (text.isNotEmpty()) {
                        // Process the text with LLM
                        processWithLlm(text, prompt)
                        return
                    }
                } catch (e: Exception) {
                    Log.e("LlmExtractViewModel", "Error reading text file: ${e.message}")
                    // Continue to server processing if direct reading fails
                }
            }

            // For all other document types, use server processing
            RetrofitClient.getApiWithToken { apiService ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        // Create a temp file
                        val extension = when (mimeType) {
                            "application/msword" -> ".doc"
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx"
                            "text/plain" -> ".txt"
                            else -> ".bin"
                        }

                        val tempFile = FileUtilsAddunit.createTempFileFromUri(
                            context,
                            fileUri,
                            "doc_",
                            extension
                        )

                        // Create multipart request
                        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                        val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                        val promptBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())

                        // Call the API
                        val response = apiService.extractTextFromDocument(filePart, promptBody)

                        // Handle response
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string() ?: ""
                            val jsonObject = JSONObject(responseBody)
                            val processedText = jsonObject.optString("processed_text", "")

                            withContext(Dispatchers.Main) {
                                _extractedText.value = processedText
                                _isLoading.value = false
                            }
                        } else {
                            throw Exception("Server error: ${response.code()} ${response.message()}")
                        }

                        // Clean up
                        tempFile.delete()
                    } catch (e: Exception) {
                        handleApiError("Document processing failed", e)
                    }
                }
            }
        } catch (e: Exception) {
            handleApiError("Document processing failed", e)
        }
    }

    /**
     * Send extracted text to LLM for processing based on user prompt
     */
    private suspend fun processWithLlm(text: String, prompt: String) {
        try {
            // Get API service with authentication
            RetrofitClient.getApiWithToken { apiService ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        // Prepare JSON payload for LLM API
                        val jsonObject = JSONObject()
                        jsonObject.put("text", text)
                        jsonObject.put("prompt", prompt)

                        val requestBody = jsonObject.toString()
                            .toRequestBody("application/json".toMediaTypeOrNull())

                        // Call LLM API
                        val response = apiService.processTextWithLlm(requestBody)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val responseText = response.body()?.string() ?: ""
                                val jsonResponse = JSONObject(responseText)
                                val processedText = jsonResponse.optString("processed_text", "")

                                _extractedText.value = processedText
                            } else {
                                _errorMessage.value = "LLM processing failed: ${response.message()}"
                            }
                            _isLoading.value = false
                        }
                    } catch (e: Exception) {
                        handleApiError("LLM API call failed", e)
                    }
                }
            }
        } catch (e: Exception) {
            handleApiError("Failed to get API service", e)
        }
    }

    /**
     * Helper to handle API errors consistently
     */
    private suspend fun handleApiError(message: String, e: Exception) {
        Log.e("LlmExtractViewModel", "$message: ${e.message}")
        withContext(Dispatchers.Main) {
            _errorMessage.value = "$message: ${e.message}"
            _isLoading.value = false
        }
    }
}