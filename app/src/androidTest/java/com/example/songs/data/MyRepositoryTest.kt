package com.example.songs.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.songs.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MyRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: SongRoomDatabase
    private lateinit var repository: SongRepository

    @Mock
    private lateinit var listDao: ListDao

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val context = InstrumentationRegistry.getInstrumentation().context
        database = Room.inMemoryDatabaseBuilder(context, SongRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = SongRepository(listDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testGetSongListWithRatings() = runBlockingTest {
        // Set up test data
        val listId = 2L
        val newArtist = Artist("New Artist")
        val song1 = Song("Sad Song", "New Artist", 123)
        val song2 = Song("Happy Song", "New Artist", 112)
        val songListSongM2M1 = SongListSongM2M(listId, song1.artistSong)
        val songListSongM2M2 = SongListSongM2M(listId, song2.artistSong)
        val songList = SongList("All Songs/Exercises", listId )
        database.songDao().insert(song1)
        database.songDao().insert(song1)
        database.listDao().insert(songList)
        database.listM2MDao().insert(songListSongM2M1)
        database.listM2MDao().insert(songListSongM2M2)

        // Set up mock to return expected result
        val expectedSongListWithSongs = SongListWithSongs(
            songList,
            listOf(song1,song2)
        )
        Mockito.`when`(listDao.getSongListWithRatings(listId.toInt())).thenReturn(expectedSongListWithSongs)

        // Call function being tested
        val resultFlow = repository.getSongListWithRatings(listId)

        // Verify that expected result is returned
        resultFlow.collect { result ->
            assert(result == expectedSongListWithSongs)
        }
    }
}
