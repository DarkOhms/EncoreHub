package com.lukemartinrecords.encorehub.data

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.adamratzman.spotify.models.SpotifySearchResult
import com.lukemartinrecords.encorehub.BuildConfig
import com.lukemartinrecords.encorehub.Constants
import com.lukemartinrecords.encorehub.api.GetSongBpm
import com.lukemartinrecords.encorehub.api.SpotifyApiHandler
import com.lukemartinrecords.encorehub.api.parseSearchJsonResult
import com.lukemartinrecords.encorehub.api.parseSearchJsonResultForSongId
import com.lukemartinrecords.encorehub.api.parseSongJsonResultForBPM
import com.lukemartinrecords.encorehub.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.json.JSONObject

/*
12/29/2021
The first iteration of the repository will only have a
 */
class SongRepository(private val songDao: SongDao, private val ratingDao: RatingDao, private val artistDao: ArtistDao, private val listDao: ListDao, private val listSongM2MDao: SongListSongM2MDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val allRatings: Flow<List<Rating>> = ratingDao.getAllRatings()
    val allArtists: LiveData<List<Artist>> = artistDao.getAllArtists()
    val allSongsWithRatings: Flow<List<SongWithRatings>> = songDao.getAllSongsWithRatings()
    val allListWithRatings: Flow<List<SongListWithRatings>> = listSongM2MDao.getAllListWithRatings()
    val albumArt: MutableLiveData<String> = MutableLiveData<String>()
    //All I need is a list of names

    val networkResults = MutableLiveData<List<Song>>()
    val builtAPI = SpotifyApiHandler()



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
    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    suspend fun searchSongs(search:String):List<Song> {

        return withContext(IO) {
            try {
                builtAPI.buildSearchApi()
                val searchResults = builtAPI.trackSearch(search)
                return@withContext parseTrackNameSearchResults(searchResults)

            }catch (e:Exception){
                e.printStackTrace()
                return@withContext emptyList()
            }
        }
            /*
            try {
                //default search limit is 10
                val response = GetSongBpm.api.search(BuildConfig.API_KEY, "song", search, 10)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonString = responseBody?.string() // Get the response body as a string
                    val jsonObject = JSONObject(jsonString) // Convert the string to a JSONObject
                    Log.d("Success!!!", " API call successful!!!")
                    if(jsonString.equals("{\"search\":{\"error\":\"no result\"}}")){
                        Log.d("No results", "No results")
                        return@withContext emptyList()
                    }else{
                        val songs = parseSearchJsonResult(jsonObject)
                        return@withContext songs

                    }
                } else {
                    Log.d("Response Failure", response.errorBody().toString())
                    return@withContext emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()
            }
        }

             */

    }
    //Stripped down version from chvia223 that just gets track names and make songs out of them
    private fun parseTrackNameSearchResults(searchResults: SpotifySearchResult): List<Song> {
        var fullResultSet: MutableList<Song> = mutableListOf()

        for (t in searchResults.tracks!!.items) {

            val singleResult = Song(t.name, 1L,-1)

            // API returns track playback time in milliseconds so this
            // converts it to the traditional m:ss display style.
            //val timeInSeconds = (t.length/1000).toInt()
            //val minutes = (timeInSeconds/60).toInt()
            //val remainderSeconds = (timeInSeconds%60).toInt()
            //val timeString = "$minutes:$remainderSeconds"
            //singleResultSet.add(timeString)

            //singleResultSet.add(t.externalUrls.spotify.toString())

            fullResultSet.add(singleResult)
        }
        return fullResultSet
    }

    private fun parseTrackArtSearchResults(searchResults: SpotifySearchResult): String {
        var fullResultSet: MutableList<String> = mutableListOf()

        for (t in searchResults.tracks!!.items) {

            val singleResult = t.album.images.first().url
            Log.d("URL", singleResult)


            fullResultSet.add(singleResult)
        }
        return fullResultSet.first().toString()
    }

    //this is a separate function because I will likely incorporate a database filter as well
    suspend fun getSongSuggestions(searchString: String): List<Song> {
        return this.searchSongs(searchString)
    }

    suspend fun getAlbumArt(songTitle: String) {
        withContext(IO) {
            try {
                builtAPI.buildSearchApi()
                val searchResults = builtAPI.trackSearch(songTitle)
                albumArt.postValue(parseTrackArtSearchResults(searchResults))

            }catch (e:Exception){
                e.printStackTrace()
                albumArt.postValue("")
            }
        }

    }

    /*
    This currently has to use 2 api calls, one to get the song id and another to get the bpm.
    Only the first has error handling.
     */
    suspend fun getBPMsFromNetwork(): ListenableWorker.Result {
        Log.d("BPM", "Getting BPMs")

        var result: ListenableWorker.Result = ListenableWorker.Result.retry()

        val songsThatNeedBpm = getSongsWithNoBpm()

        if (songsThatNeedBpm.isEmpty()) {
            result = ListenableWorker.Result.success()
        }

        for (song in songsThatNeedBpm) {

            withContext(IO) {
                try {

                    val searchResponse =
                        GetSongBpm.api.search(Constants.API_KEY, "song", song.songTitle, 1)
                    if (searchResponse.isSuccessful) {
                        Log.d("Success!!!", " API call successful!!!")
                        val responseBody = searchResponse.body()
                        val jsonSearchString = responseBody?.string() // Get the response body as a string
                        val jsonSearchObject = JSONObject(jsonSearchString) // Convert the string to a JSONObject
                        if (jsonSearchString.equals("{\"search\":{\"error\":\"no result\"}}")) {
                            Log.d("No results", "No results")
                        } else {
                            val songId = parseSearchJsonResultForSongId(jsonSearchObject)

                            val bpmResponse = GetSongBpm.api.song(Constants.API_KEY, songId)
                            val bpmResponseBody = bpmResponse.body()

                            if(bpmResponseBody == null){
                                Log.d("BPM", "BPM response body is null")
                            }else {

                                val bpm = bpmResponseBody.getSongBPM()
                                if (bpm > 0) {
                                    val songWithBpm = song.copy(bpm = bpm)
                                    updateSong(songWithBpm)
                                    result = ListenableWorker.Result.success()
                                } else {
                                    result = ListenableWorker.Result.failure()
                                }
                            }

                        }
                    } else {
                        Log.d("Response Failure", searchResponse.errorBody().toString())
                        result = ListenableWorker.Result.retry()
                    }
                } catch (e: Exception) {
                    Log.d("BPM Exception", e.toString())
                    e.printStackTrace()
                    result = ListenableWorker.Result.retry()
                }
            }
        }
        return result
    }

    private suspend fun getSongsWithNoBpm(): List<Song> {
        Log.d("BPM", "Getting songs with no bpm")
        val allSongs = allSongs.first()
        val songsWithNoBpm = allSongs.filter { song -> song.bpm < 1 }
        return songsWithNoBpm
    }
}

