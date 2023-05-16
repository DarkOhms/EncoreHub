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
            "WHERE id = :songId")
    fun getSong(songId: Long): Song

    @Query("SELECT * FROM song_table")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song): Long

    @Query("DELETE FROM song_table")
    fun deleteAll()

    //using artistSong to delete unique to the artist/user
    @Query("DELETE FROM SONG_TABLE WHERE id = :songId")
    suspend fun deleteSong(songId: Long)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSong(song: Song)

}