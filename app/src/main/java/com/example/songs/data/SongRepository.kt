package com.example.songs.data

import androidx.annotation.WorkerThread
import com.example.songs.model.Artist
import com.example.songs.model.Rating
import com.example.songs.model.Song
import com.example.songs.model.SongWithRatings
import kotlinx.coroutines.flow.Flow
/*
12/29/2021
The first iteration of the repository will only have a
 */
class SongRepository(private val songDao: SongDao, private val ratingDao: RatingDao, private val artistDao: ArtistDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val allRatings: Flow<List<Rating>> = ratingDao.getAllRatings()
    val allSongsWithRatings: Flow<List<SongWithRatings>> = songDao.getAllSongsWithRatings()
    //All I need is a list of names


    //this puts off the artist selection until later, currently at ViewModel creation
    //7/8/22 new strategy:  filter allSongs to create all songs with ratings in viewModel

    fun getArtistSongsWithRatings(artist:String):Flow<List<SongWithRatings>>{
       return songDao.getArtistSongsWithRatings(artist)
    }


    fun getSong(songTitle: String, artist: String): Song {
        return songDao.getSong(songTitle, artist)
    }



    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    suspend fun  getArtistList(): List<String> = artistDao.getArtistList()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertSong(song: Song) {
        songDao.insert(song)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertRating(rating: Rating) {
        ratingDao.insert(rating)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertArtist(artist: Artist) {
        artistDao.insert(artist)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        songDao.deleteAll()
        ratingDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteSong(song: String) {
        songDao.deleteSong(song)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

}