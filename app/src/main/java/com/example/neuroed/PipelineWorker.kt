import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.neuroed.model.CallAgent
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.RetrofitClient
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class PipelineWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getInt("user_id", -1)
            .takeIf { it >= 0 } ?: return Result.failure()

        val payload = CallAgent(UserId = userId, time = System.currentTimeMillis().toString())

        return try {
            // Use ApiHelper to get the token and make the authenticated API call
            ApiHelper.executeWithToken { token ->
                RetrofitClient.apiService.callAgent(userId, payload, token)
            }

            Log.d("PipelineWorker", "Agent called successfully for userId=$userId")

            // ✅ Reschedule itself after 5 seconds
            scheduleNext(userId)

            Result.success()

        } catch (e: HttpException) {
            Log.e("PipelineWorker", "HttpException: ${e.message}")
            Result.retry()
        }

    }

    private fun scheduleNext(userId: Int) {
        val inputData = workDataOf("user_id" to userId)

        val request = OneTimeWorkRequestBuilder<PipelineWorker>()
            .setInitialDelay(10, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
        Log.d("PipelineWorker", "Rescheduled for 5 seconds later.")
    }
}
