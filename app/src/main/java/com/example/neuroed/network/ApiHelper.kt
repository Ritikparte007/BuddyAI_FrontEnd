// ApiHelper.kt - पूरा कोड
package com.example.neuroed.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object ApiHelper {
    private const val TAG = "ApiHelper"


    private var cachedToken: String? = null
    private var tokenExpiryTimeMs: Long = 0

    private const val TOKEN_EXPIRY_BUFFER_MS = 60_000L

    /**
     * Firebase से ID Token प्राप्त करने के लिए सस्पेंड फंक्शन।
     * यह टोकन को कैश करता है और केवल जरूरत पड़ने पर ही नया टोकन फेच करता है।
     *
     * @return Firebase ID Token या null यदि यूजर लॉगिन नहीं है
     * @throws Exception यदि टोकन प्राप्त करने में कोई समस्या आती है
     */
    suspend fun getFirebaseToken(): String? {
        // चेक करें कि क्या टोकन कैश किया गया है और वैलिड है
        val currentTimeMs = System.currentTimeMillis()
        if (cachedToken != null && currentTimeMs < tokenExpiryTimeMs - TOKEN_EXPIRY_BUFFER_MS) {
            Log.d(TAG, "Using cached token")
            return cachedToken
        }

        // नया टोकन प्राप्त करें
        return suspendCancellableCoroutine { continuation ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Log.d(TAG, "No user logged in")
                cachedToken = null
                tokenExpiryTimeMs = 0
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token

                    // Firebase टोकन आमतौर पर 1 घंटे के लिए वैलिड होते हैं
                    // यदि expirationTime उपलब्ध नहीं है, तो 1 घंटा डिफॉल्ट मान का उपयोग करें
                    val expirationTimeMs = System.currentTimeMillis() + 3600 * 1000

                    // टोकन और एक्सपायरी टाइम कैश करें
                    cachedToken = token
                    tokenExpiryTimeMs = expirationTimeMs

                    Log.d(TAG, "New Firebase token received and cached")
                    continuation.resume(token)
                } else {
                    Log.e(TAG, "Error getting token", task.exception)

                    // एरर होने पर कैश क्लियर करें
                    cachedToken = null
                    tokenExpiryTimeMs = 0

                    continuation.resumeWithException(
                        task.exception ?: IllegalStateException("Unknown error getting token")
                    )
                }
            }

            continuation.invokeOnCancellation {
                Log.d(TAG, "Token request cancelled")
            }
        }
    }

    /**
     * Firebase Token के साथ API कॉल को एग्जीक्यूट करता है।
     * यह फंक्शन पहले token प्राप्त करता है और फिर उसे प्रदान किए गए ब्लॉक में पास करता है।
     *
     * @param block सस्पेंड फंक्शन जो Token लेता है और T टाइप का रिजल्ट लौटाता है
     * @return ब्लॉक से रिटर्न किया गया रिजल्ट
     * @throws IllegalStateException यदि यूजर ऑथेंटिकेटेड नहीं है
     * @throws Exception यदि API कॉल विफल होती है
     */
    suspend fun <T> executeWithToken(block: suspend (String) -> T): T {
        val token = getFirebaseToken() ?: throw IllegalStateException("User not authenticated")
        return block("Bearer $token")
    }

    /**
     * यूजर के लॉगिन स्टेटस के आधार पर ऑप्शनल API कॉल एग्जीक्यूट करता है।
     *
     * @param authenticatedBlock यदि यूजर ऑथेंटिकेटेड है तो एग्जीक्यूट होने वाला ब्लॉक
     * @param guestBlock [ऑप्शनल] यदि यूजर ऑथेंटिकेटेड नहीं है तो एग्जीक्यूट होने वाला ब्लॉक
     * @return ब्लॉक से रिटर्न किया गया रिजल्ट या guestBlock नहीं दिया गया है तो null
     */
    suspend fun <T> executeIfAuthenticated(
        authenticatedBlock: suspend (String) -> T,
        guestBlock: (suspend () -> T)? = null
    ): T? {
        val token = getFirebaseToken()
        return if (token != null) {
            authenticatedBlock("Bearer $token")
        } else {
            guestBlock?.invoke()
        }
    }

    /**
     * यह मेथड टोकन कैश को मैन्युअली क्लियर करने के लिए है।
     * ऐप लॉगआउट के दौरान इसे कॉल किया जाना चाहिए।
     */
    fun clearTokenCache() {
        cachedToken = null
        tokenExpiryTimeMs = 0
        Log.d(TAG, "Token cache cleared")
    }

    /**
     * यूजर के लॉगिन स्टेटस की जांच करता है।
     *
     * @return true अगर यूजर लॉगिन है, false अन्यथा
     */
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * वर्तमान यूजर का Firebase UID प्राप्त करता है।
     *
     * @return Firebase UID या null यदि यूजर लॉगिन नहीं है
     */
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}