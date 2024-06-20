package com.lukemartinrecords.encorehub.model

import androidx.room.Entity
import androidx.room.PrimaryKey
/*
5/22/2022
I may change this because it allows for duplicate artists.
 */
@Entity(tableName = "artist_table")
data class Artist(
    var name: String?,

    ) {
    @PrimaryKey(autoGenerate = true)
    //this makes the artistID 1 when it isn't inserted into the DB
    var artistId: Long = 0

}

