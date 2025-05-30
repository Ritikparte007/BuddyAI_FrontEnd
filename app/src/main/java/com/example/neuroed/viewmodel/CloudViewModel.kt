package com.example.neuroed.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.*
import com.example.neuroed.repository.CloudRepository
import com.example.neuroed.utils.CloudFileOperation
import com.example.neuroed.utils.Resource
import com.example.neuroed.utils.StorageFilterOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudViewModel(
    private val repository: CloudRepository,
    private val userId: Int
) : ViewModel() {

    // Files list state
    private val _files = MutableStateFlow<List<CloudItem>>(emptyList())
    val files: StateFlow<List<CloudItem>> = _files.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Loading message
    private val _loadingMessage = MutableStateFlow("")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    // Current operation state
    private val _currentOperation = MutableStateFlow<CloudFileOperation?>(null)
    val currentOperation: StateFlow<CloudFileOperation?> = _currentOperation.asStateFlow()

    // Current filter
    private val _currentFilter = MutableStateFlow(StorageFilterOption.ALL)
    val currentFilter: StateFlow<StorageFilterOption> = _currentFilter.asStateFlow()

    // Status message
    private val _statusMessage = MutableStateFlow<Pair<String, Boolean>?>(null)
    val statusMessage: StateFlow<Pair<String, Boolean>?> = _statusMessage.asStateFlow()

    // Recent items
    private val _recentItems = MutableStateFlow<Resource<List<CloudItem>>>(Resource.Loading())
    val recentItems: StateFlow<Resource<List<CloudItem>>> = _recentItems.asStateFlow()

    // Storage summary
    private val _storageSummary = MutableStateFlow<Resource<StorageSummary>>(Resource.Loading())
    val storageSummary: StateFlow<Resource<StorageSummary>> = _storageSummary.asStateFlow()

    // Current folder
    private val _currentFolderId = MutableStateFlow<String?>(null)
    val currentFolderId: StateFlow<String?> = _currentFolderId.asStateFlow()

    // Pagination
    private var currentPage = 1
    private var hasMorePages = true

    init {
        loadFiles()
        loadRecentActivity()
        loadStorageSummary()
    }

    /**
     * Load files with current filter and folder
     */
    fun loadFiles() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Loading files..."

            repository.getFiles(
                userId = userId,
                folderId = _currentFolderId.value,
                filter = _currentFilter.value,
                page = 1,
                pageSize = 20
            ).collect { result ->
                result.onSuccess { fileList ->
                    _files.value = fileList ?: emptyList()
                    currentPage = 1
                    hasMorePages = (fileList?.size ?: 0) >= 20
                    Log.d("CloudViewModel", "Loaded ${fileList?.size ?: 0} files")
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error loading files", exception)
                    showStatusMessage("Failed to load files: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Load next page of files
     */
    fun loadNextPage() {
        if (_isLoading.value || !hasMorePages) return

        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Loading more files..."

            repository.getFiles(
                userId = userId,
                folderId = _currentFolderId.value,
                filter = _currentFilter.value,
                page = currentPage + 1,
                pageSize = 20
            ).collect { result ->
                result.onSuccess { fileList ->
                    val newFiles = fileList ?: emptyList()
                    _files.value = _files.value + newFiles
                    currentPage++
                    hasMorePages = newFiles.size >= 20
                    Log.d("CloudViewModel", "Loaded ${newFiles.size} more files")
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error loading more files", exception)
                    showStatusMessage("Failed to load more files: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Apply filter to files
     */
    fun applyFilter(filter: StorageFilterOption) {
        _currentFilter.value = filter
        loadFiles()
    }

    /**
     * Search files by query
     */
    fun searchFiles(query: String) {
        if (query.isBlank()) {
            loadFiles()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Searching files..."

            repository.searchFiles(userId, query).collect { result ->
                result.onSuccess { fileList ->
                    _files.value = fileList ?: emptyList()
                    Log.d("CloudViewModel", "Found ${fileList?.size ?: 0} files for query: $query")
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error searching files", exception)
                    showStatusMessage("Search failed: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Navigate to folder
     */
    fun navigateToFolder(folderId: String) {
        _currentFolderId.value = folderId
        loadFiles()
    }

    /**
     * Navigate back to parent folder
     */
    fun navigateBack() {
        // Implement folder navigation logic here
        _currentFolderId.value = null
        loadFiles()
    }

    /**
     * Load recent activity
     */
    fun loadRecentActivity() {
        viewModelScope.launch {
            _recentItems.value = Resource.Loading()

            repository.getRecentActivity(userId, 10).collect { result ->
                result.onSuccess { items ->
                    _recentItems.value = Resource.Success(items)
                    Log.d("CloudViewModel", "Loaded ${items?.size ?: 0} recent items")
                }.onFailure { exception ->
                    _recentItems.value = Resource.Error("Failed to load recent activity", exception)
                    Log.e("CloudViewModel", "Error loading recent activity", exception)
                }
            }
        }
    }

    /**
     * Load storage summary
     */
    fun loadStorageSummary() {
        viewModelScope.launch {
            _storageSummary.value = Resource.Loading()

            repository.getStorageSummary(userId).collect { result ->
                result.onSuccess { summary ->
                    _storageSummary.value = Resource.Success(summary)
                    Log.d("CloudViewModel", "Loaded storage summary")
                }.onFailure { exception ->
                    _storageSummary.value = Resource.Error("Failed to load storage summary", exception)
                    Log.e("CloudViewModel", "Error loading storage summary", exception)
                }
            }
        }
    }

    /**
     * Upload file
     */
    fun uploadFile(context: Context, uri: Uri, folderId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Uploading file..."

            repository.uploadFile(context, uri, userId, folderId).collect { result ->
                result.onSuccess { response ->
                    if (response?.success == true) {
                        showStatusMessage("File uploaded successfully", true)
                        loadFiles() // Refresh files list
                        loadStorageSummary() // Refresh storage summary
                    } else {
                        showStatusMessage(response?.message ?: "Upload failed", false)
                    }
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error uploading file", exception)
                    showStatusMessage("Upload failed: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Create folder
     */
    fun createFolder(folderName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Creating folder..."

            repository.createFolder(userId, folderName, _currentFolderId.value).collect { result ->
                result.onSuccess { response ->
                    if (response?.success == true) {
                        showStatusMessage("Folder created successfully", true)
                        loadFiles() // Refresh files list
                    } else {
                        showStatusMessage(response?.message ?: "Failed to create folder", false)
                    }
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error creating folder", exception)
                    showStatusMessage("Failed to create folder: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }





    // CloudViewModel.kt - UPDATED VERSION

// Update these specific methods in your CloudViewModel:

    /**
     * Download file - CORRECTED with userId
     */
    fun downloadFile(context: Context, fileId: String, fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Downloading file..."

            repository.downloadFile(context, fileId, fileName, userId).collect { result ->
                result.onSuccess { success ->
                    if (success) {
                        showStatusMessage("File downloaded successfully", true)
                    } else {
                        showStatusMessage("Download failed", false)
                    }
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error downloading file", exception)
                    showStatusMessage("Download failed: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete item - CORRECTED with userId
     */
    fun deleteItem(fileId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Deleting item..."

            repository.deleteFile(fileId, userId).collect { result ->
                result.onSuccess { response ->
                    if (response?.success == true) {
                        showStatusMessage("Item deleted successfully", true)
                        loadFiles() // Refresh files list
                        loadStorageSummary() // Refresh storage summary
                    } else {
                        showStatusMessage(response?.message ?: "Delete failed", false)
                    }
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error deleting item", exception)
                    showStatusMessage("Delete failed: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Rename item - CORRECTED with userId
     */
    fun renameItem(fileId: String, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Renaming item..."

            repository.renameFile(fileId, newName, userId).collect { result ->
                result.onSuccess { response ->
                    if (response?.success == true) {
                        showStatusMessage("Item renamed successfully", true)
                        loadFiles() // Refresh files list
                    } else {
                        showStatusMessage(response?.message ?: "Rename failed", false)
                    }
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error renaming item", exception)
                    showStatusMessage("Rename failed: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Share item - CORRECTED with userId
     */
    fun shareItem(fileId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Generating share link..."

            repository.shareFile(fileId, userId).collect { result ->
                result.onSuccess { response ->
                    if (response?.success == true) {
                        showStatusMessage("Share link generated successfully", true)
                        // You can add logic here to copy the share URL to clipboard
                        // or show it in a dialog
                    } else {
                        showStatusMessage(response?.message ?: "Share failed", false)
                    }
                }.onFailure { exception ->
                    Log.e("CloudViewModel", "Error sharing item", exception)
                    showStatusMessage("Share failed: ${exception.message}", false)
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Set current operation
     */
    fun setCurrentOperation(operation: CloudFileOperation?) {
        _currentOperation.value = operation
    }

    /**
     * Clear current operation
     */
    fun clearCurrentOperation() {
        _currentOperation.value = null
    }

    /**
     * Show status message
     */
    private fun showStatusMessage(message: String, isSuccess: Boolean) {
        _statusMessage.value = Pair(message, isSuccess)
    }

    /**
     * Clear status message
     */
    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    /**
     * Refresh all data
     */
    fun refreshData() {
        loadFiles()
        loadRecentActivity()
        loadStorageSummary()
    }
}