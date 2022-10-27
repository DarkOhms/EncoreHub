package com.example.songs.model

import android.util.Log
import androidx.lifecycle.*
import com.example.songs.data.SongRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class SongViewModel(private val repository: SongRepository) : ViewModel() {
    // Using LiveData and caching what allSongsWithRatings returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.


    //default artist name for this phase of production
    var artistName = "Ear Kitty"
    private val _artistNameLive = MutableLiveData<String>(artistName)
    val artistNameLive: LiveData<String>
        get() = _artistNameLive


    private val _allSongsWithRatings: MutableLiveData<List<SongWithRatings>> =
        repository.allSongsWithRatings.asLiveData() as MutableLiveData<List<SongWithRatings>>

    val allSongsWithRatings: LiveData<List<SongWithRatings>>
        get() = _allSongsWithRatings

    private val _allArtistSongsWithRatings = (artistNameLive.value?.let {
        repository.getArtistSongsWithRatings(
            it
        ).asLiveData()
    }) as MutableLiveData<List<SongWithRatings>>

    val allArtistSongsWithRatings: LiveData<List<SongWithRatings>>
        get() = _allArtistSongsWithRatings

    private var temp = mutableListOf<SongWithRatings>()
    private val _practiceList = MutableLiveData<List<SongWithRatings>>()

    val practiceList: LiveData<List<SongWithRatings>>
        get() = _practiceList

    init {
        _practiceList.value = temp
    }


    fun getArgumentSong(songTitle: String): SongWithRatings{
        val argSong = allArtistSongsWithRatings.value?.find { songWithRatings: SongWithRatings ->
            songTitle == songWithRatings.song.songTitle

        }
        return argSong!!
    }

    fun getArtistList(onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val artists = withContext(Dispatchers.IO) { repository.getArtistList() }

            // maybe validate data, e.g. not empty

            onResult(artists)
        }
    }

    fun changeArtist(artist: String) {
        _artistNameLive.value = artist
        artistName = artist
        initializeWithArtist()

    }

    /*
    fun updateAllSongs() = viewModelScope.launch {
        run {
            _allSongs = repository.getArtistSongsWithRatings(artistNameLive.value.toString())
                .asLiveData() as MutableLiveData<List<SongWithRatings>>
        }
    }

     */


    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * these all interact with the repository
     */
    fun insertSong(song: Song) = viewModelScope.launch {
        repository.insertSong(song)
    }

    fun insertRating(rating: Rating) = viewModelScope.launch {
        repository.insertRating(rating)
        sortByTimestamp()
    }

    fun insertArtist(artist: Artist) = viewModelScope.launch {
        repository.insertArtist(artist)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun deleteSong(song: String) = viewModelScope.launch {
        repository.deleteSong(song)
    }

    fun updateSongNotes(oldSongWithRatings: SongWithRatings, notes: String) =
        viewModelScope.launch {

            val updatedSong = oldSongWithRatings.song.copy(songNotes = notes)
            repository.updateSong(updatedSong)
        }
    /*
    end repository functions
     */

    fun getSongTitles(): ArrayList<String>
    {
        val songTitles = arrayListOf<String>()

        val iterator = allSongsWithRatings.value!!.listIterator()

        while (iterator.hasNext())
        songTitles.add(iterator.next().song.songTitle)

        return songTitles.distinct().toCollection(ArrayList<String>())

   }

    fun getTempo(songTitle: String): Int{
        val song = allSongsWithRatings.value?.find { songWithRatings: SongWithRatings ->
            songTitle == songWithRatings.song.songTitle

        }
        return song?.song?.bpm ?: 0

    }

    fun isReady():Boolean{
        return !allSongsWithRatings.value.isNullOrEmpty()
    }
    fun initializeWithArtist(){

        var newList = mutableListOf<SongWithRatings>()
        newList = allSongsWithRatings.value as MutableList<SongWithRatings>

        //make sure the list isn't empty
        if(newList.isNullOrEmpty()){
            //handle empty list error
        }else{
            //list sorted by performance rating
            _allArtistSongsWithRatings.value = newList.filter { it.song.artistName == artistNameLive.value  } as MutableList<SongWithRatings>
            allArtistSongsWithRatings.value

        }

    }
    /**
     * Business logic for creating lists
     * moved to SongViewModel from CreateSongList class
     */

    /*
This class will contain the algorithms for song list creation.
12/6/2021
Some use case examples based on list size:
Song number
1 trivial example
2 easy song first unless both songs were already played that day
    if already played that session, harder song first
3 easy song first


variables to consider
current performance rating
performance frequency
performance freshness
A side, B side tempo

7 songs paradigm for practice

1st high performance rating, freshly played, A side or high tempo
2nd high performance rating, medium freshness, A side or high tempo
3rd medium performance rating, freshly played
4th medium to high performance rating, medium freshness, B side or low tempo
5th high performance rating, low freshness rating, A side or high tempo
6th lowest performance rating, high freshness rating
7th lowest performance rating of the previous 6
 */
    fun createPracticeList(){

        var newList = mutableListOf<SongWithRatings>()
        newList = allArtistSongsWithRatings.value as MutableList<SongWithRatings>

        //make sure the list isn't empty
        if(newList.isNullOrEmpty()){
            //handle empty list error
        }else{
            //sort list by performance rating
            //update song ratings
            newList.forEach(){
                it.recentPerformanceRating()
            }
            //list sorted by performance rating
            _practiceList.value = newList.sortedByDescending{ it.recentPerformanceRating() } as MutableList<SongWithRatings>
            practiceList.value

        }

    }
    fun sortByTimestamp(){
        var newList = mutableListOf<SongWithRatings>()
        newList = allArtistSongsWithRatings.value as MutableList<SongWithRatings>

        //make sure the list isn't empty
        if(newList.isNullOrEmpty()){
            //handle empty list error
        }else{
            //sort list by performance rating
            //update song ratings
            newList.forEach(){
                it.lastPlayed()
            }
            //list sorted by performance rating
            _practiceList.value = newList.sortedBy { it.lastPlayed() } as MutableList<SongWithRatings>
            practiceList.value
        }
    }


}
class SongViewModelFactory(private val repository: SongRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SongViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return SongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}