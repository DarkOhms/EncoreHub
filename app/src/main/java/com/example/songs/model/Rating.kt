package com.example.songs.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.songs.data.Converters
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/*
12/30/3021
converting Rating to a database entity

4/9/2022
Updating database schema to include artist, instrument and quick rating
artistName  TEXT NOT NULL DEFAULT "Ear Kitty"
quickRate  BOOL DEFAULT TURE treated as an INTEGER in sqllite
 */
@Entity(tableName = "ratings_table")
data class Rating(

                  @ColumnInfo(name = "timeStamp")
                  val timeStamp: Long,
                  @ColumnInfo(name = "songTitle")
                  val songTitle: String,
                  @ColumnInfo(name = "artistName", defaultValue = "Ear Kitty")
                  val artistName: String,
                  @ColumnInfo(name = "rating")
                  val rating: Int){
    @PrimaryKey(autoGenerate = true)
    var ratingId: Long? =null

    @ColumnInfo(defaultValue = "1")
    var isQuickRate: Boolean = true

    @ColumnInfo(defaultValue = "")
    var ratingNotes: String = ""
    //dirty way for artistSong
    var artistSong: String? = artistName + songTitle

    fun ratingTimeString(): String{
        val raw : Long = this.timeStamp
        val date = Date(raw)
        val dateFormat = SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault())
        return dateFormat.format(raw)
    }
}
