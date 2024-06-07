package com.example.songs.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.asLiveData
import com.example.songs.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

/*
12/29/2021
The first iteration of the repository will only have a
 */
class SongRepository(private val songDao: SongDao, private val ratingDao: RatingDao, private val artistDao: ArtistDao, private val listDao: ListDao, private val listSongM2MDao: SongListSongM2MDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val allRatings: Flow<List<Rating>> = ratingDao.getAllRatings()
    val allArtists: Flow<List<Artist>> = artistDao.getAllArtists()
    val allSongsWithRatings: Flow<List<SongWithRatings>> = songDao.getAllSongsWithRatings()
    val allListWithRatings: Flow<List<SongListWithRatings>> = listSongM2MDao.getAllListWithRatings()
    //All I need is a list of names


    //this puts off the artist selection until later, currently at ViewModel creation
    //7/8/22 new strategy:  filter allSongs to create all songs with ratings in viewModel

    fun getArtistSongsWithRatings(artistId:Long):Flow<List<SongWithRatings>>{
       return songDao.getArtistSongsWithRatings(artistId)
    }

    //list functions
    //12/12/2022 retrieve artist lists

    fun gatArtistSongLists(artistId:Long):Flow<List<ArtistLists>>{
        return listDao.getArtistLists(artistId)
    }

    fun gatSongListWithRatings(listId:Long):Flow<SongListWithRatings>{
        return listSongM2MDao.getSongListWithRatings(listId)
    }

    fun getArtistListsWithRatings(artistId: Long): Flow<List<SongListWithRatings>>{
        return listSongM2MDao.getArtistListsWithRatings(artistId)
    }
    fun getArtistLists(artistId: Long):Flow<List<ArtistLists>>{
        return listDao.getArtistLists(artistId)
    }
    //end list functions

    fun getSong(songId:Long): Song {
        return songDao.getSong(songId)
    }



    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    suspend fun  getArtistNameList(): List<String> = artistDao.getArtistNameList()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertSong(song: Song):Long {
        return songDao.insert(song)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertList(newList: SongList):Long {
        return listDao.insert(newList)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertListAssociation(newListAssociation: SongListSongM2M) {
        listSongM2MDao.insert(newListAssociation)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertRating(rating: Rating) {
        ratingDao.insert(rating)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertArtist(artist: Artist):Long {
        return artistDao.insert(artist)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        songDao.deleteAll()
        ratingDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteSong(songId:Long) {
        songDao.deleteSong(songId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

}