package com.example.songs.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
12/30/3021
converting Rating to a database entity

4/9/2022
Updating database schema to include artist, instrument and quick rating
artistName  TEXT NOT NULL DEFAULT "Ear Kitty"
quickRate  BOOL DEFAULT TURE treated as an INTEGER in sqlite

6/4/2024
Adding cascade delete and song foreign key
 */
@Entity(tableName = "ratings_table",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["songId"], //foreign key
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE // Cascade delete from Song to Rating
        )
    ],
    indices = [Index(value = ["songId"])]
)
data class Rating(

                  @ColumnInfo(name = "timeStamp") val timeStamp: Long,
                  @ColumnInfo(name = "songId") val songId: Long,
                  @ColumnInfo(name = "rating") val rating: Int)
{
    @PrimaryKey(autoGenerate = true)
    var ratingId: Long? =null

    @ColumnInfo(defaultValue = "1")
    var isQuickRate: Boolean = true

    @ColumnInfo(defaultValue = "")
    var ratingNotes: String = ""

    fun ratingTimeString(): String{
        val raw : Long = this.timeStamp
        val date = Date(raw)
        val dateFormat = SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault())
        return dateFormat.format(raw)
    }
}
