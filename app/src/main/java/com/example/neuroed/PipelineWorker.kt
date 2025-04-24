package com.example.neuroed

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.neuroed.model.CallAgent
import com.example.neuroed.network.RetrofitClient
import retrofit2.HttpException

class PipelineWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getInt("user_id", -1)
            .takeIf { it >= 0 } ?: return Result.failure()

        val payload = CallAgent(UserId = userId, time = System.currentTimeMillis().toString())

        return try {
            RetrofitClient.apiService.callAgent(userId, payload)
            Result.success()

        } catch (e: HttpException) {
            Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

}
