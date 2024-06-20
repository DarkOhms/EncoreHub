package com.lukemartinrecords.encorehub.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "song_list_joining_table",
    primaryKeys = ["listId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["songId"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongList::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    //this will ensure that the association is deleted if the song or list is deleted
    ],
    indices = [
        Index(value = ["songId"]),
        Index(value = ["listId"])
    ])
data class SongListSongM2M(
    val listId: Long,
    val songId: Long
)
