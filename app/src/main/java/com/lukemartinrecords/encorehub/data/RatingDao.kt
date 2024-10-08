package com.lukemartinrecords.encorehub.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lukemartinrecords.encorehub.model.Rating
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {

    @Query("SELECT * FROM ratings_table")
    fun getAllRatings(): Flow<List<Rating>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rating: Rating)

    @Query("DELETE FROM ratings_table")
    suspend fun deleteAll()
}