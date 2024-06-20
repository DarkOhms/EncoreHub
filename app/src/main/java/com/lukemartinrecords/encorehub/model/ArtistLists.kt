package com.lukemartinrecords.encorehub.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import kotlin.collections.List

//Adding set list capability 12/9/2022, requires a db upgrade

@Entity
data class ArtistLists(
    @Embedded val artist:Artist,
    @Relation(
        parentColumn = "artistId",
        entityColumn = "artistId"
    )
    val lists: List<SongList>
)
