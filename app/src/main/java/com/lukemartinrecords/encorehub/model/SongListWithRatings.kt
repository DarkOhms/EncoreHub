package com.lukemartinrecords.encorehub.model

import androidx.room.*

@Entity
data class SongListWithRatings(
    @Embedded val setList: SongList,
    @Relation(
        entity = Song::class,
        parentColumn = "listId",
        entityColumn = "songId",
        associateBy = Junction(SongListSongM2M::class)
    )
    val songList: List<SongWithRatings>

){
    /*12/15/2022
    This seems ridiculous but apparently I can't embed the SongWithRatings
    because they are an entity with a relation so I need this extra step.
    This function filters the artist's songs with ratings by their match to the set list

    fun getSetListWithRatings(artistSongsWithRatings:List<SongWithRatings> ):List<SongWithRatings>{
        return artistSongsWithRatings.filter { p -> songList.any { it.artistSong == p.song.artistSong }  }
    }

     */
}
