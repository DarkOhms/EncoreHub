package com.lukemartinrecords.encorehub.model

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.nanoseconds

class SongWithRatingsTest {

    private lateinit var originalLocale: Locale
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUpLocale() {
        originalLocale = Locale.getDefault()
        originalTimeZone = TimeZone.getDefault()
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun recentPerformanceRatingIsZeroWithoutRatings() {
        assertEquals(0, songWithRatings().recentPerformanceRating())
    }

    @Test
    fun recentPerformanceRatingUsesTheOnlyRating() {
        assertEquals(83, songWithRatings(rating(1_000L, 83)).recentPerformanceRating())
    }

    @Test
    fun recentPerformanceRatingWeightsTwoRatingsTowardTheSecondEntry() {
        assertEquals(
            80,
            songWithRatings(rating(1_000L, 50), rating(2_000L, 100)).recentPerformanceRating()
        )
    }

    @Test
    fun recentPerformanceRatingUsesTheLastThreeRatings() {
        assertEquals(
            71,
            songWithRatings(
                rating(1_000L, 0),
                rating(2_000L, 10),
                rating(3_000L, 20),
                rating(4_000L, 100)
            ).recentPerformanceRating()
        )
    }

    @Test
    fun lastPlayedUsesZeroAndNeverForAnUnratedSong() {
        val song = songWithRatings()

        assertEquals(0.nanoseconds, song.lastPlayed())
        assertEquals("Never", song.lastPlayedString())
    }

    @Test
    fun lastPlayedUsesTheFinalRatingTimestamp() {
        val song = songWithRatings(rating(0L, 50), rating(1_000_000_000L, 70))

        assertEquals(1_000_000_000L.nanoseconds, song.lastPlayed())
        assertEquals("01/12/70 01:46 PM", song.lastPlayedString())
    }

    private fun songWithRatings(vararg ratings: Rating): SongWithRatings = SongWithRatings(
        song = Song(songTitle = "Test song", artistId = 1L, bpm = 120),
        ratingHistory = ratings.toList()
    )

    private fun rating(timestamp: Long, value: Int) = Rating(timestamp, 1L, value)
}
