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
6/4/2024
Long overdue database update to support proper foreign keys and cascading delete
 */
@Entity(tableName = "song_table",
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["artistId"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE // Cascade delete from Artist to Song
        )
    ],
    indices = [
        Index(value = ["songTitle", "artistId"], unique = true),
        Index(value = ["artistId"])
    ]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val songId: Long = 0,
    val songTitle: String,
    val artistId: Long,
    val bpm: Int,
    val songNotes: String,

){
    //constructor without bpm
    constructor(songTitle: String, artistId: Long) : this(0, songTitle, artistId, 321, "")
    //constructor with bpm
    constructor(songTitle: String, artistId: Long, bpm: Int ) : this(0,songTitle, artistId, bpm, "")
}
