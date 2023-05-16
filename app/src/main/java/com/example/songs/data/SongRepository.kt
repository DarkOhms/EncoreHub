package com.example.songs.data

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.asLiveData
import com.example.songs.model.*
import kotlinx.coroutines.flow.*

/*
12/29/2021
The first iteration of the repository will only have a
 */
class SongRepository(private val songDao: SongDao, private val ratingDao: RatingDao, private val artistDao: ArtistDao, private val listDao: ListDao, private val listSongM2MDao: SongListSongM2MDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    val allSongsWithRatings: Flow<List<SongWithRatings>> = songDao.getAllSongsWithRatings()
    val allArtists: Flow<List<Artist>> = artistDao.getAllArtists()
    val allListWithSongs: Flow<List<SongListWithSongs>> = listSongM2MDao.getAllListWithSongs()
    val allArtistWithListsAndSongs: Flow<List<ArtistWithListsAndSongs>> = artistDao.getArtistsWithPlaylistsAndSongs()
    //All I need is a list of names

    // Initialize currentArtistFilter with a default mapping function
    //default values
    var artistName = "First Not Showing"
    //
    private val currentArtistFilter = MutableStateFlow<(List<ArtistWithListsAndSongs>) -> ArtistWithListsAndSongs> { artists ->
        artists.firstOrNull() ?: ArtistWithListsAndSongs(Artist(name = artistName), emptyList())
    }

    val currentArtistWithListsAndSongs: Flow<ArtistWithListsAndSongs> = combine(
        allArtistWithListsAndSongs,
        currentArtistFilter
    ) { artists, filter ->
        filter.invoke(artists)
    }

    fun setCurrentArtistById(artistId: Long) {
        currentArtistFilter.value = { artists -> artists.find { it.artist.artistId == artistId } ?: artists.firstOrNull() ?: ArtistWithListsAndSongs(Artist(artistName), emptyList()) }
    }

    fun setCurrentArtistByName(artistName: String) {
        Log.d("Repository setCurrentArtistByName", "method called to change to " + artistName + " is " + artistName.length.toString())
        currentArtistFilter.value = {
                artists ->
            Log.d("Repository filter", "Artist for filter is: " + artistName )

            artists.find { it.artist.name == artistName }?: ArtistWithListsAndSongs(Artist("Filter Broken"), emptyList())
        }
    }

    //this puts off the artist selection until later, currently at ViewModel creation
    //7/8/22 new strategy:  filter allSongs to create all songs with ratings in viewModel

    fun getArtistSongsWithRatings(artist:String):Flow<List<SongWithRatings>>{
       return songDao.getArtistSongsWithRatings(artist)
    }

    //list functions
    //12/12/2022 retrieve artist lists



    fun getCurrentSongs(){

    }
    fun getSongListWithRatings(listId:Int):Flow<SongListWithSongs>{
        return listDao.getSongListWithRatings(listId)
    }

    fun getArtistListsWithRatings(artistId: Long): Flow<List<SongListWithSongs>>{
        return listSongM2MDao.getArtistListsWithRatings(artistId)
    }
    //end list functions

    fun getSong(songId: Long ): Song {
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
    suspend fun deleteSong(songId: Long) {
        songDao.deleteSong(songId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

}