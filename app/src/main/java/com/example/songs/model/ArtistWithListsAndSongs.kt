package com.example.songs.model

import androidx.room.Embedded
import androidx.room.Relation

/*
2/28/23
This is a a third level of nested relation that forces room to pull a lot of data
from the database at once and make many associations.  We will see how this effects
performance.  Hopefully it is only a performance burden on the initial load.
 */
data class ArtistWithListsAndSongs(
    @Embedded val artist: Artist,
    @Relation(
        entity = SongList::class,
        parentColumn = "artistId",
        entityColumn = "artistId"
    )
    val artistLists: List<SongListWithSongs>
)
