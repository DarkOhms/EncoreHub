package com.example.songs.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(tableName = "song_list_joining_table",
    foreignKeys = [
        ForeignKey(entity = SongList::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = CASCADE),
        ForeignKey(entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = CASCADE)
    ],
    primaryKeys = ["listId", "id"]
)
data class SongListSongM2M(
    val listId: Long,
    val id: Long
)
