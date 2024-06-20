package com.lukemartinrecords.encorehub.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lukemartinrecords.encorehub.model.Artist

/**
 * 4/14/2022
 * I've opted to do the heavy lifting in the songs Dao with fun getArtistSongsWithRatings
 * I don't know yet if it will work or if it is best practice to do it that way so it may
 * be temporary.  This Dao may just be the lightweight way of getting a list of artists.
 */
@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist_table")
    fun getAllArtists(): LiveData<List<Artist>>

    //gets a list of artist names
    @Query("SELECT name FROM artist_table")
    fun getArtistNameList(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: Artist): Long
}