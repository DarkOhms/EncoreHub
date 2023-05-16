package com.example.songs.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ArtistDaoTest {
    private lateinit var database: SongRoomDatabase
    private lateinit var repository: SongRepository
    private lateinit var dao: ArtistDao

    @Before
    fun setup(){

        //main thread queries
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SongRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.artistDao()
    }

    @After
    fun tearDown(){
        database.close()
    }

    @Test
    fun insertArtist() = runBlocking {

    }
}