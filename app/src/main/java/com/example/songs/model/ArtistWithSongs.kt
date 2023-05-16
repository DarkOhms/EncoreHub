package com.example.songs.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

/**
 * 4/14/2022
 * Continued work on major database upgrade
 */
@Entity
data class ArtistWithSongs(
    @Embedded val artist:Artist,
    @Relation(
        parentColumn = "artistId",
        entityColumn = "artistId"
    )
    val songs: List<SongWithRatings>
)
