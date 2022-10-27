package com.example.songs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.songs.model.Artist
import kotlinx.coroutines.flow.Flow

/**
 * 4/14/2022
 * I've opted to do the heavy lifting in the songs Dao with fun getArtistSongsWithRatings
 * I don't know yet if it will work or if it is best practice to do it that way so it may
 * be temporary.  This Dao may just be the lightweight way of getting a list of artists.
 */
@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist_table")
    fun getAllArtists(): Flow<List<Artist>>

    @Query("SELECT name FROM artist_table")
    fun getArtistList(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: Artist)
}