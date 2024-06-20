package com.lukemartinrecords.encorehub.data

import androidx.annotation.WorkerThread
import com.lukemartinrecords.encorehub.model.Rating
import kotlinx.coroutines.flow.Flow

class RatingsRepository(private val ratingDao: RatingDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allRatings: Flow<List<Rating>> = ratingDao.getAllRatings()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(rating: Rating) {
        ratingDao.insert(rating)
    }
}