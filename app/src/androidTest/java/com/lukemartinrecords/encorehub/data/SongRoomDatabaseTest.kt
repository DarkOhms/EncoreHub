package com.lukemartinrecords.encorehub.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lukemartinrecords.encorehub.model.Artist
import com.lukemartinrecords.encorehub.model.Rating
import com.lukemartinrecords.encorehub.model.Song
import com.lukemartinrecords.encorehub.model.SongList
import com.lukemartinrecords.encorehub.model.SongListSongM2M
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongRoomDatabaseTest {

    private lateinit var database: SongRoomDatabase
    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            SongRoomDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun songRelationsAreIsolatedByArtist() = runBlocking {
        val firstArtistId = database.artistDao().insert(Artist("First artist"))
        val secondArtistId = database.artistDao().insert(Artist("Second artist"))
        val firstSongId = database.songDao().insert(Song("Shared title", firstArtistId, 90))
        val secondSongId = database.songDao().insert(Song("Shared title", secondArtistId, 100))
        database.ratingDao().insert(Rating(1L, firstSongId, 40))
        database.ratingDao().insert(Rating(2L, secondSongId, 90))

        val firstArtistSongs = database.songDao().getArtistSongsWithRatings(firstArtistId).first()
        val secondArtistSongs = database.songDao().getArtistSongsWithRatings(secondArtistId).first()

        assertEquals(listOf(firstSongId), firstArtistSongs.map { it.song.songId })
        assertEquals(listOf(secondSongId), secondArtistSongs.map { it.song.songId })
        assertEquals(40, firstArtistSongs.single().ratingHistory.single().rating)
        assertEquals(90, secondArtistSongs.single().ratingHistory.single().rating)
    }

    @Test
    fun duplicateSongTitleIsIgnoredWithinAnArtist() = runBlocking {
        val artistId = database.artistDao().insert(Artist("Artist"))
        val firstInsert = database.songDao().insert(Song("Song", artistId, 90))
        val duplicateInsert = database.songDao().insert(Song("Song", artistId, 120))

        assertTrue(firstInsert > 0L)
        assertEquals(-1L, duplicateInsert)
        assertEquals(1, database.songDao().getArtistSongsWithRatings(artistId).first().size)
    }

    @Test
    fun listsContainOnlyTheirJoinedSongs() = runBlocking {
        val artistId = database.artistDao().insert(Artist("Artist"))
        val songOneId = database.songDao().insert(Song("One", artistId, 90))
        val songTwoId = database.songDao().insert(Song("Two", artistId, 100))
        val listId = database.listDao().insert(SongList("Performance", artistId))
        database.listM2MDao().insert(SongListSongM2M(listId, songTwoId))

        val list = database.listM2MDao().getSongListWithRatings(listId).first()

        assertEquals(listOf(songTwoId), list.songList.map { it.song.songId })
        assertTrue(list.songList.none { it.song.songId == songOneId })
    }

    @Test
    fun deletingSongCascadesToRatingsAndSetListMemberships() = runBlocking {
        val artistId = database.artistDao().insert(Artist("Artist"))
        val songId = database.songDao().insert(Song("Song", artistId, 90))
        val listId = database.listDao().insert(SongList("Set list", artistId))
        database.ratingDao().insert(Rating(1L, songId, 70))
        database.listM2MDao().insert(SongListSongM2M(listId, songId))

        database.songDao().deleteSong(database.songDao().getSong(songId))

        assertTrue(database.ratingDao().getAllRatings().first().isEmpty())
        assertTrue(database.listM2MDao().getSongListWithRatings(listId).first().songList.isEmpty())
    }
}
