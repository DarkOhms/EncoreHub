package com.example.songs.data

import androidx.room.*
import com.example.songs.model.SongListSongM2M
import com.example.songs.model.SongListWithSongs
import com.example.songs.model.SongWithRatings
import kotlinx.coroutines.flow.Flow

@Dao
interface SongListSongM2MDao {
    //this adds a song to a list
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(join: SongListSongM2M)

    /*
    12/15/2022
    I don't know which of the next two is best practice but
    I will start by working with the 2nd option and doing the filtering in my viewmodel.
     */

    /*
    3/21/23
    I am trying strategy 1 where I am using allSongsWithRatings as my live data to be
    pushed to the view and using the lists inside the viewmodel to filter it.
    Below is where I will get all the lists and I will use that for filtering.
     */
    @Transaction
    @Query("SELECT * FROM list_table ")
    fun getAllListWithSongs(): Flow<List<SongListWithSongs>>

    @Transaction
    @Query("SELECT * FROM list_table "+
            "WHERE artistId = :artistId")
    fun getArtistListsWithRatings(artistId: Long): Flow<List<SongListWithSongs>>
}