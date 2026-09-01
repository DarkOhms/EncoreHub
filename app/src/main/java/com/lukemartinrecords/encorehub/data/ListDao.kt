package com.lukemartinrecords.encorehub.data

import androidx.room.*
import com.lukemartinrecords.encorehub.model.SongList

@Dao
interface ListDao {
    @Query("SELECT listId FROM list_table WHERE artistId = :artistId AND listName = :listName LIMIT 1")
    suspend fun getListId(artistId: Long, listName: String): Long?

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

}
