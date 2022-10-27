package com.example.songs.model

import androidx.room.Entity
import androidx.room.PrimaryKey
/*
5/22/2022
I may change this because it allows for duplicate artists.
 */
@Entity(tableName = "artist_table")
data class Artist(
    val name: String,

) {
    @PrimaryKey(autoGenerate = true)
    var artistId: Long? =null
}

