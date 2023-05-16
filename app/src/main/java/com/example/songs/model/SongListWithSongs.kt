package com.example.songs.model

import androidx.room.*

@Entity
data class SongListWithSongs(
    @Embedded val setList: SongList,
    @Relation(
        entity = Song::class,
        parentColumn = "listId",
        entityColumn = "id",
        associateBy = Junction(SongListSongM2M::class)
    )
    val songList: List<Song>

)
