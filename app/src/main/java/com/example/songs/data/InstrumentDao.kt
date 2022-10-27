package com.example.songs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.songs.model.Instrument
import com.example.songs.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentDao {

    @Query("SELECT * FROM instrument_table" +
            " WHERE artist = :artistName")
    fun getArtistInstruments(artistName: String): Flow<List<Instrument>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(instrument: Instrument)
}