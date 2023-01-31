package com.example.songs.data

import androidx.room.*
import com.example.songs.model.ArtistLists
import com.example.songs.model.SongList
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    /*
    12/12/2022

    lists do not exist independently from Artists and Songs so I decided
    to make list retrieval a function of the ArtistDao.  So far I will
    only use the ListDao for insertion, deletion, and update of lists.
     */

    //inserts a relation between an artist and a list name
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(list: SongList):Long

    //deletes a list by ID
    @Query("DELETE FROM list_table WHERE listId = :id")
    suspend fun deleteList(id: Long)

    //gets artist song lists
    @Transaction
    @Query("SELECT * FROM list_table"+
            " WHERE artistId = :artistId")
    fun getArtistLists(artistId: Long): Flow<List<ArtistLists>>
}