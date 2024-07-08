package com.lukemartinrecords.encorehub

import android.app.Application
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lukemartinrecords.encorehub.data.SongRepository
import com.lukemartinrecords.encorehub.data.SongRoomDatabase
import com.lukemartinrecords.encorehub.utils.PracticeWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class EncoreHubApplication: Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { SongRoomDatabase.getDatabase(this, applicationScope) }

    val repository by lazy { SongRepository(database.songDao(), database.ratingDao(),
        database.artistDao(), database.listDao(), database.listM2MDao() ) }
    override fun onCreate() {
        super.onCreate()
        delayedInit()

    }

    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    //this is specifically for just creating notifications but likely to expand on this in the future
    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .apply {
                    setRequiresDeviceIdle(true)
            }.build()

        val repeatingRequest
                = PeriodicWorkRequestBuilder<PracticeWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            PracticeWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)
    }
}