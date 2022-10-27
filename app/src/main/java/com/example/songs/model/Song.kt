package com.example.songs.model

import androidx.room.*

/*
12/28/2021
I am converting the project with it's stub code to implement the Room Database for persistence.
I seems I will need two tables, one for the songs themselves and one for the rating history.

4/9/2022
First attempt at a simple database migration adding notes to the song entity.
Success.

Now for a much more useful migration adding artist name and turning it into a composite key
 */
@Entity(tableName = "song_table", primaryKeys = ["songTitle", "artistName"])
data class Song(
     val songTitle: String,
     val artistName: String,
     val bpm: Int,
     val songNotes: String,
     val artistSong: String,

){
    //constructor without bpm
    constructor(songTitle: String, artistName: String) :this(songTitle, artistName, 321, "",artistName+songTitle)
    //constructor with bpm
    constructor(songTitle: String, artistName: String, bpm: Int ) :this(songTitle, artistName, bpm, "",artistName+songTitle)
}
