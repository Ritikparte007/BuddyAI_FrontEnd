// Feed.kt
import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayInputStream
import android.graphics.BitmapFactory
//import androidx.compose.ui.graphics.ImageBitmap

/**
 * Data model for Feed
 */
data class Feed(
    val id: Int,
    val title: String?,
    val content: String?,
    val imageBase64: String?,
    val agentType: String?,  // Make sure this is nullable
    val contentType: String?, // Make sure this is nullable
    val createdAt: String?,
    val spendingTimeSeconds: Int?
) {
    // Convert to UI model for display
    fun toNewsContent(): NewsContent {
        return NewsContent(
            id = id,
            title = title ?: "Untitled",
            description = content ?: "No content",
            imageRes = 0, // Will be replaced with decoded bitmap
            imageBitmap = decodeBase64Image(),
            source = getSourceFromContentType(),
            timePosted = getFormattedTime(),
            likes = 0 // Not tracked in API model
        )
    }

    // Decode base64 image to bitmap
    private fun decodeBase64Image(): ImageBitmap? {
        return try {
            imageBase64?.let {
                val imageBytes = Base64.decode(it, Base64.DEFAULT)
                val inputStream = ByteArrayInputStream(imageBytes)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get source name from content type
    private fun getSourceFromContentType(): String {
        return when (contentType) {
            "Technology" -> "Tech News"
            "Chemistry" -> "Chemistry Today"
            "Biology" -> "Bio Science"
            "Physics" -> "Physics World"
            else -> "NeuroEd"
        }
    }

    // Format the timestamp for display
    private fun getFormattedTime(): String {
        // Simple formatting logic - can be enhanced
        return try {
            val now = System.currentTimeMillis()
            val posted = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .parse(createdAt)?.time ?: now

            val diffInMillis = now - posted
            val diffInHours = diffInMillis / (1000 * 60 * 60)

            when {
                diffInHours < 1 -> "Just now"
                diffInHours < 24 -> "$diffInHours hours ago"
                else -> "${diffInHours / 24} days ago"
            }
        } catch (e: Exception) {
            "Recently"
        }
    }
}


// NewsContent.kt


data class NewsContent(
    val id: Int,
    val title: String,
    val description: String,
    val imageRes: Int = 0, // For resource images
    val imageBitmap: ImageBitmap? = null, // For API images
    val source: String,
    val timePosted: String,
    val likes: Int
)


enum class AgentType(val value: String) {
    NEWS_AGENT("NewsAgent"),
    BUDDY_AGENT("BuddyAgent")
}

// Content Type Enum
enum class ContentType(val value: String) {
    TECHNOLOGY("Technology"),
    CHEMISTRY("Chemistry"),
    BIOLOGY("Biology"),
    PHYSICS("Physics"),
    OTHER("Other")
}