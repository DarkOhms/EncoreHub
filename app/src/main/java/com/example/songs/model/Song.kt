package com.example.songs.model

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE

/*
12/28/2021
I am converting the project with it's stub code to implement the Room Database for persistence.
I seems I will need two tables, one for the songs themselves and one for the rating history.

4/9/2022
First attempt at a simple database migration adding notes to the song entity.
Success.

Now for a much more useful migration adding artist name and turning it into a composite key

5/9/23
We are making a major change by adding an auto generated primary key in the form of id
so that we can clean up relationships to ratings, lists and artist.  This requires
the addition of artistId as a foreign key allows us to do away with artistSong.

Artist -> Song -> Rating
       -> SongList

 */
@Entity
(tableName = "song_table",
    foreignKeys = [
        ForeignKey(entity = Artist::class,
        parentColumns = ["artistId"],
        childColumns = ["artistId"],
        onDelete = CASCADE)
        ],
    indices = [
        Index(value = ["artistId"])
    ]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songTitle: String,
    val artistName: String,
    val artistId: Long,
    val bpm: Int,
    val songNotes: String,


){
    //constructor without bpm
    constructor(songTitle: String, artistName: String, artistId: Long) :this(0,songTitle, artistName, artistId,321, "")
    //constructor with bpm
    constructor(songTitle: String, artistName: String, artistId: Long,bpm: Int ) :this(0,songTitle, artistName, artistId, bpm, "")
}
