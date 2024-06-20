package com.lukemartinrecords.encorehub.model

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
        parentColumn = "name",
        entityColumn = "artistName"
    )
    val songs: List<SongWithRatings>
)
