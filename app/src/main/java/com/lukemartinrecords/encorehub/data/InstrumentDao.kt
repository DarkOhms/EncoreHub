package com.lukemartinrecords.encorehub.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lukemartinrecords.encorehub.model.Instrument
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentDao {

    @Query("SELECT * FROM instrument_table" +
            " WHERE artist = :artistName")
    fun getArtistInstruments(artistName: String): Flow<List<Instrument>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(instrument: Instrument)
}