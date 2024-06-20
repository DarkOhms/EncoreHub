package com.lukemartinrecords.encorehub.data

import androidx.room.*
import com.lukemartinrecords.encorehub.model.SongListSongM2M
import com.lukemartinrecords.encorehub.model.SongListWithRatings
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
    @Transaction
    @Query("SELECT * FROM list_table " +
            "WHERE listId = :listId")
    fun getSongListWithRatings(listId:Long): Flow<SongListWithRatings>

    @Transaction
    @Query("SELECT * FROM list_table ")
    fun getAllListWithRatings(): Flow<List<SongListWithRatings>>

    @Transaction
    @Query("SELECT * FROM list_table "+
            "WHERE artistId = :artistId")
    fun getArtistListsWithRatings(artistId: Long): Flow<List<SongListWithRatings>>
}