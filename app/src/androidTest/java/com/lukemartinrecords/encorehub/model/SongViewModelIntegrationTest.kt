package com.lukemartinrecords.encorehub.model

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lukemartinrecords.encorehub.EncoreHubApplication
import com.lukemartinrecords.encorehub.data.SongRepository
import com.lukemartinrecords.encorehub.data.SongRoomDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class SongViewModelIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var database: SongRoomDatabase
    private lateinit var repository: SongRepository
    private lateinit var preferences: EncoreHubApplication.PreferencesManager
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context
        context.getSharedPreferences("EncoreHubPrefs", Context.MODE_PRIVATE).edit().clear().commit()
        database = Room.inMemoryDatabaseBuilder(context, SongRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SongRepository(
            database.songDao(),
            database.ratingDao(),
            database.artistDao(),
            database.listDao(),
            database.listM2MDao()
        )
        preferences = EncoreHubApplication.PreferencesManager(context)
        viewModelStore = ViewModelStore()
    }

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { viewModelStore.clear() }
        database.close()
        context.getSharedPreferences("EncoreHubPrefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun restoresArtistAndMasterListAndPersistsListChanges() = runBlocking {
        val artistId = database.artistDao().insert(Artist("Artist"))
        val masterListId = database.listDao().insert(SongList("All Songs/Exercises", artistId))
        val customListId = database.listDao().insert(SongList("Performance", artistId))
        preferences.setCurrentArtistId(artistId)
        preferences.setCurrentListId(customListId)

        val viewModel = createViewModel()

        assertEquals(artistId, viewModel.currentArtistLive.getOrAwaitValue().artistId)
        assertEquals(customListId, viewModel.currentListIdLive.getOrAwaitValue())

        runOnMain { viewModel.changeList(masterListId) }

        assertEquals(masterListId, viewModel.currentListIdLive.getOrAwaitValue { it == masterListId })
        assertEquals(masterListId, preferences.getCurrentListId())
    }

    @Test
    fun newSongIsAddedToItsArtistsMasterList() = runBlocking {
        val artistId = database.artistDao().insert(Artist("Artist"))
        val masterListId = database.listDao().insert(SongList("All Songs/Exercises", artistId))
        val customListId = database.listDao().insert(SongList("Custom", artistId))
        preferences.setCurrentArtistId(artistId)
        preferences.setCurrentListId(customListId)
        val viewModel = createViewModel()
        viewModel.currentArtistLive.getOrAwaitValue()

        runOnMain { viewModel.insertSong(Song("New song", artistId, 100)) }

        val masterList = withTimeout(5_000L) {
            database.listM2MDao().getSongListWithRatings(masterListId).first {
                it.songList.any { song -> song.song.songTitle == "New song" }
            }
        }
        val customList = database.listM2MDao().getSongListWithRatings(customListId).first()

        assertEquals(listOf("New song"), masterList.songList.map { it.song.songTitle })
        assertTrue(customList.songList.isEmpty())
    }

    @Test
    fun creatingAListAssociatesTheSelectedSongsAndMakesItCurrent() = runBlocking {
        val artistId = database.artistDao().insert(Artist("Artist"))
        val masterListId = database.listDao().insert(SongList("All Songs/Exercises", artistId))
        val songId = database.songDao().insert(Song("Selected song", artistId, 100))
        database.listM2MDao().insert(SongListSongM2M(masterListId, songId))
        preferences.setCurrentArtistId(artistId)
        preferences.setCurrentListId(masterListId)
        val viewModel = createViewModel()
        val currentArtist = viewModel.currentArtistLive.getOrAwaitValue()
        viewModel.selectedSongs = database.songDao()
            .getArtistSongsWithRatings(artistId)
            .first()
            .toMutableList()

        runOnMain { viewModel.createNewList("Performance", currentArtist) }

        val createdList = withTimeout(5_000L) {
            database.listM2MDao().getArtistListsWithRatings(artistId).first { lists ->
                lists.any { list ->
                    list.setList.listName == "Performance" &&
                        list.songList.any { song -> song.song.songId == songId }
                }
            }.single { it.setList.listName == "Performance" }
        }

        assertEquals(listOf(songId), createdList.songList.map { it.song.songId })
        assertEquals(
            createdList.setList.listId,
            viewModel.currentListIdLive.getOrAwaitValue { it == createdList.setList.listId }
        )
    }

    @Test
    fun practiceAndPerformListsUseTheCurrentSetList() = runBlocking {
        val artistId = database.artistDao().insert(Artist("Artist"))
        val masterListId = database.listDao().insert(SongList("All Songs/Exercises", artistId))
        val lowSongId = database.songDao().insert(Song("Low", artistId, 80))
        val middleSongId = database.songDao().insert(Song("Middle", artistId, 90))
        val highSongId = database.songDao().insert(Song("High", artistId, 100))
        database.ratingDao().insert(Rating(1L, lowSongId, 60))
        database.ratingDao().insert(Rating(2L, middleSongId, 80))
        database.ratingDao().insert(Rating(3L, highSongId, 90))
        listOf(lowSongId, middleSongId, highSongId).forEach { songId ->
            database.listM2MDao().insert(SongListSongM2M(masterListId, songId))
        }
        preferences.setCurrentArtistId(artistId)
        preferences.setCurrentListId(masterListId)
        val viewModel = createViewModel()

        assertEquals(
            listOf("High", "Middle", "Low"),
            viewModel.practiceListLive.getOrAwaitValue { it.size == 3 }.map { it.song.songTitle }
        )
        assertEquals(
            listOf("High", "Middle"),
            viewModel.performanceListLive.getOrAwaitValue { it.size == 2 }.map { it.song.songTitle }
        )

        runOnMain { viewModel.setRatingFilterValue(85) }

        assertEquals(
            listOf("High"),
            viewModel.performanceListLive.getOrAwaitValue { it.size == 1 }.map { it.song.songTitle }
        )
    }

    private fun createViewModel(): SongViewModel {
        lateinit var viewModel: SongViewModel
        runOnMain {
            viewModel = ViewModelProvider(
                viewModelStore,
                SongViewModelFactory(repository, preferences)
            )[SongViewModel::class.java]
        }
        return viewModel
    }

    private fun runOnMain(block: () -> Unit) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
    }

    private fun <T> LiveData<T>.getOrAwaitValue(
        timeout: Long = 5_000L,
        predicate: (T) -> Boolean = { true }
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        lateinit var observer: Observer<T>
        observer = Observer { value ->
            if (value != null && predicate(value)) {
                data = value
                latch.countDown()
                removeObserver(observer)
            }
        }
        observeForever(observer)

        if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
            removeObserver(observer)
            throw TimeoutException("LiveData value was not received within $timeout ms")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}
