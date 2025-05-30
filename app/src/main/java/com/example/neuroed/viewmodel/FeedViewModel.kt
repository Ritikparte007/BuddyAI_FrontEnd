// FeedViewModel.kt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.neuroed.model.AgentType
//import com.example.neuroed.model.ContentType
//import com.example.neuroed.model.Feed
import com.example.neuroed.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling Feed-related operations and UI state
 */
class FeedViewModel(private val repository: FeedRepository) : ViewModel() {

    // StateFlow for list of feeds
    private val _feedsState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedsState: StateFlow<FeedUiState> = _feedsState.asStateFlow()

    // Currently selected feed
    private val _selectedFeed = MutableStateFlow<Feed?>(null)
    val selectedFeed: StateFlow<Feed?> = _selectedFeed.asStateFlow()

    init {
        loadFeeds()
    }

    /**
     * Fetches all feeds from the repository
     */
    fun loadFeeds() {
        viewModelScope.launch {
            _feedsState.value = FeedUiState.Loading

            repository.getFeeds().fold(
                onSuccess = { feeds ->
                    if (feeds.isEmpty()) {
                        _feedsState.value = FeedUiState.Empty
                    } else {
                        _feedsState.value = FeedUiState.Success(feeds)
                    }
                },
                onFailure = { error ->
                    _feedsState.value = FeedUiState.Error(error.message ?: "Unknown error occurred")
                }
            )
        }
    }

    /**
     * Loads feeds filtered by agent type
     */
    fun loadFeedsByAgentType(type: AgentType) {
        viewModelScope.launch {
            _feedsState.value = FeedUiState.Loading

            repository.getFeedsByAgentType(type).fold(
                onSuccess = { feeds ->
                    if (feeds.isEmpty()) {
                        _feedsState.value = FeedUiState.Empty
                    } else {
                        _feedsState.value = FeedUiState.Success(feeds)
                    }
                },
                onFailure = { error ->
                    _feedsState.value = FeedUiState.Error(error.message ?: "Unknown error occurred")
                }
            )
        }
    }

    /**
     * Loads feeds filtered by content type
     */
    fun loadFeedsByContentType(type: ContentType) {
        viewModelScope.launch {
            _feedsState.value = FeedUiState.Loading

            repository.getFeedsByContentType(type).fold(
                onSuccess = { feeds ->
                    if (feeds.isEmpty()) {
                        _feedsState.value = FeedUiState.Empty
                    } else {
                        _feedsState.value = FeedUiState.Success(feeds)
                    }
                },
                onFailure = { error ->
                    _feedsState.value = FeedUiState.Error(error.message ?: "Unknown error occurred")
                }
            )
        }
    }

    /**
     * Updates time spent viewing a feed
     */
    fun updateTimeSpent(id: Int, seconds: Int) {
        viewModelScope.launch {
            repository.updateTimeSpent(id, seconds)
            // No need to update UI state for this operation
        }
    }

    /**
     * Selects a feed
     */
    fun selectFeed(feed: Feed) {
        _selectedFeed.value = feed
    }
}

/**
 * UI state for the Feed screen
 */
sealed class FeedUiState {
    object Loading : FeedUiState()
    object Empty : FeedUiState()
    data class Success(val feeds: List<Feed>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}