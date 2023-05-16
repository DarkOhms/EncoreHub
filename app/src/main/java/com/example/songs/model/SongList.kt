package com.example.songs.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey


/*
This only contains the name of the list and the artist to whom it belongs.

Because we have a many to many relationship between the artist lists and the artist
songs, we will need a joining table and an entity that looks more like the actual
list of songs.

4/3/23
I am adding the foreign key so that when an artist is deleted, so are their lists.
 */
@Entity(tableName = "list_table",
    foreignKeys = [
        ForeignKey(entity = Artist::class,
            parentColumns = ["artistId"],
            childColumns = ["artistId"],
            onDelete = CASCADE)
    ]
)
data class SongList(
    val listName: String,
    val artistId: Long,
    @PrimaryKey(autoGenerate = true)
    var listId: Long = 0
)
