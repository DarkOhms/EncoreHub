package com.lukemartinrecords.encorehub.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.asLiveData
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lukemartinrecords.encorehub.EncoreHubApplication
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.Duration

class PracticeWorker(appContext: Context, workerParams: WorkerParameters):CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PracticeWorker"
    }
    override suspend fun doWork(): Result {
        Log.d("PracticeWorker", "doWork called")
        val application = applicationContext as EncoreHubApplication
        //I'll test this with a default artist
        val songs = application.repository.getArtistSongsWithRatings(1).firstOrNull()
        if (songs.isNullOrEmpty()) {
            Log.d("PracticeWorker", "No songs found")
            return Result.failure()
        }else{
            val currentTimeMillis = System.currentTimeMillis()
            val currentTime = Instant.ofEpochMilli(currentTimeMillis)
            //current practice interval for testing is within 1 day
            val hasPracticedRecently = songs.any { Duration.between(Instant.ofEpochMilli(it.lastPlayed().inWholeMilliseconds), currentTime).toDays() < 2 }
            //send a notification to practice if not practiced recently
            if (!hasPracticedRecently) {
                sendNotification(applicationContext)
            }

        }
        return Result.success()


    }

}