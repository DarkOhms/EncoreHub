package com.example.songs.data

import androidx.room.*
import com.example.songs.model.ArtistWithListsAndSongs
import com.example.songs.model.SongList
import com.example.songs.model.SongListWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    /*
    12/12/2022

    lists do not exist independently from Artists and Songs so I decided
    to make list retrieval a function of the ArtistDao.  So far I will
    only use the ListDao for insertion, deletion, and update of lists.
     */

    @Query("SELECT * FROM list_table")
    fun getAllLists(): Flow<List<SongList>>

    //inserts a relation between an artist and a list name
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(list: SongList):Long

    //deletes a list by ID
    @Query("DELETE FROM list_table WHERE listId = :id")
    suspend fun deleteList(id: Long)

    @Transaction
    @Query("SELECT * FROM list_table " +
            "WHERE listId = :listId")
    fun getSongListWithRatings(listId:Int): Flow<SongListWithSongs>
}