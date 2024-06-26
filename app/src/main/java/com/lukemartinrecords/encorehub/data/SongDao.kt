package com.lukemartinrecords.encorehub.data

import androidx.room.*
import com.lukemartinrecords.encorehub.model.Song
import com.lukemartinrecords.encorehub.model.SongWithRatings
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    //rather than having get artist with songs I decided to try it all in this transaction
    @Transaction
    @Query("SELECT * FROM song_table "+
    "WHERE artistId = :artistId")
    fun getArtistSongsWithRatings(artistId: Long): Flow<List<SongWithRatings>>

    @Transaction
    @Query("SELECT * FROM song_table ")
    fun getAllSongsWithRatings(): Flow<List<SongWithRatings>>


    @Query("SELECT * FROM song_table "+
            "WHERE songId = :songId")
    fun getSong(songId: Long): Song

    @Query("SELECT * FROM song_table")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song): Long

    @Query("DELETE FROM song_table")
    fun deleteAll()

    //now appropriately using the songId to delete the song
    @Delete
    suspend fun deleteSong(song: Song)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSong(song: Song)

}