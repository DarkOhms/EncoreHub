package com.lukemartinrecords.encorehub.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lukemartinrecords.encorehub.EncoreHubApplication

class BPMWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "BPMWorker"
    }
    override suspend fun doWork(): Result {
        Log.d("BPMWorker", "doWork called")
        val application = applicationContext as EncoreHubApplication

        return application.repository.getBPMsFromNetwork()
    }

}