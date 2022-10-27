package com.example.songs.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instrument_table")
data class Instrument(val artist: String, val instrument:String){
    @PrimaryKey(autoGenerate = true)
    var instrumentId: Long? =null
}
