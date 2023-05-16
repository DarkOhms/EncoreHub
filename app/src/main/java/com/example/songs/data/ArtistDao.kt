package com.example.songs.data

import androidx.room.*
import com.example.songs.model.Artist
import com.example.songs.model.ArtistWithListsAndSongs
import kotlinx.coroutines.flow.Flow

/**
 * 4/14/2022
 * I've opted to do the heavy lifting in the songs Dao with fun getArtistSongsWithRatings
 * I don't know yet if it will work or if it is best practice to do it that way so it may
 * be temporary.  This Dao may just be the lightweight way of getting a list of artists.
 */
@Dao
interface ArtistDao {

    @Transaction
    @Query("SELECT * FROM artist_table")
    fun getArtistsWithPlaylistsAndSongs(): Flow<List<ArtistWithListsAndSongs>>

    @Transaction
    @Query("SELECT * FROM artist_table "+
            "WHERE artistId = :artistId")
    fun getArtistWithPlaylistsAndSongs(artistId: Long): Flow<ArtistWithListsAndSongs>

    @Transaction
    @Query("SELECT * FROM artist_table "+
            "WHERE artistId = :artistId")
    fun changeArtistWithPlaylistsAndSongs(artistId: Long): ArtistWithListsAndSongs

    @Query("SELECT * FROM artist_table")
    fun getAllArtists(): Flow<List<Artist>>

    //gets a list of artist names
    @Query("SELECT name FROM artist_table")
    fun getArtistNameList(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: Artist): Long
}