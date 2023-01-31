package com.example.songs.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_list_joining_table", primaryKeys = ["listId", "artistSong"])
data class SongListSongM2M(
    val listId: Long,
    val artistSong: String
)
