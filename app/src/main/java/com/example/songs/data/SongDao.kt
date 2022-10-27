package com.example.songs.data

import androidx.room.*
import com.example.songs.model.Song
import com.example.songs.model.SongWithRatings
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    //rather than having get artist with songs I decided to try it all in this transaction
    @Transaction
    @Query("SELECT * FROM song_table "+
    "WHERE artistName = :artistName")
    fun getArtistSongsWithRatings(artistName: String): Flow<List<SongWithRatings>>

    @Transaction
    @Query("SELECT * FROM song_table ")
    fun getAllSongsWithRatings(): Flow<List<SongWithRatings>>


    @Query("SELECT * FROM song_table "+
            "WHERE artistName = :artistName & songTitle = :song")
    fun getSong(song: String, artistName: String): Song

    @Query("SELECT * FROM song_table")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song)

    @Query("DELETE FROM song_table")
    fun deleteAll()

    @Query("DELETE FROM SONG_TABLE WHERE songTitle = :song")
    fun deleteSong(song: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSong(song: Song)

}