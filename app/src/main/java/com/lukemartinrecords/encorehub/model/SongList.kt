package com.lukemartinrecords.encorehub.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


/*
This only contains the name of the list and the artist to whom it belongs.

Because we have a many to many relationship between the artist lists and the artist
songs, we will need a joining table and an entity that looks more like the actual
list of songs.
 */
@Entity (tableName = "list_table",
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["artistId"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE // Cascade delete from Artist to SongList
        )
    ],
    indices = [
            Index(value = ["listName", "artistId"], unique = true), // Unique constraint on listName and artistId
            Index(value = ["artistId"])  // Index on artistId
    ]
)
data class SongList(
    val listName: String,
    val artistId: Long // Foreign key referencing Artist
)
{
    @PrimaryKey(autoGenerate = true)
    var listId: Long = 0

}
