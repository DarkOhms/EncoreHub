package com.example.songs.model

import androidx.room.Entity
import androidx.room.PrimaryKey


/*
This only contains the name of the list and the artist to whom it belongs.

Because we have a many to many relationship between the artist lists and the artist
songs, we will need a joining table and an entity that looks more like the actual
list of songs.
 */
@Entity (tableName = "list_table")
data class SongList(
    val listName: String,
    val artistId: Long
)
{
    @PrimaryKey(autoGenerate = true)
    var listId: Long = 0

}
