package com.example.neuroed.network





import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

        // आपके द्वारा अपडेट किए गए URL को बनाए रखें - make it public
        const val BASE_URL = "http://127.0.0.1:8000/"

        // लॉगिंग इंटरसेप्टर - सभी HTTP लॉग्स देखने के लिए
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // बेसिक OkHttpClient - PLACEHOLDER_TOKEN वाले authInterceptor को हटा दिया
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)  // केवल लॉगिंग
            .build()

        // बेसिक API सर्विस - ऑथेंटिकेशन के बिना
        val apiService: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        // वास्तविक फायरबेस टोकन के साथ API सर्विस प्राप्त करें
        fun getApiWithToken(callback: (ApiService) -> Unit) {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Log.d("RetrofitClient", "No user logged in")
                callback(apiService)
                return
            }

            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    if (token != null) {
                        Log.d("RetrofitClient", "Firebase token received: ${token.substring(0, Math.min(20, token.length))}...")

                        // टोकन के साथ नया OkHttpClient बनाएं
                        val tokenAuthClient = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(loggingInterceptor)
                            .addInterceptor { chain ->
                                val original = chain.request()
                                val request = original.newBuilder()
                                    .header("Authorization", "Bearer $token")
                                    .method(original.method, original.body)
                                    .build()

                                Log.d("RetrofitClient", "Sending request with Auth header: Bearer ${token.substring(0, Math.min(20, token.length))}...")
                                chain.proceed(request)
                            }
                            .build()

                        // टोकन के साथ नया API सर्विस बनाएं
                        val tokenApiService = Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(tokenAuthClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                            .create(ApiService::class.java)

                        callback(tokenApiService)
                    } else {
                        Log.e("RetrofitClient", "Token is null")
                        callback(apiService)
                    }
                } else {
                    Log.e("RetrofitClient", "Error getting token", task.exception)
                    callback(apiService)
                }
            }
        }
    }