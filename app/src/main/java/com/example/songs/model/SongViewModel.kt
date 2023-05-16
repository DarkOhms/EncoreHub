package com.example.songs.model

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import com.example.songs.data.SongRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*
import kotlin.collections.ArrayList

class SongViewModel(private val repository: SongRepository) : ViewModel() {
    // Using LiveData and caching what allSongsWithRatings returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.


    /*
    12/19/2022
    This will need some updating.  I am transitioning to using artistId because
    it will scale better but right now much is connected to the artistName parameter.

    For now I will have to load from the artist_table to find the ID of by artistName.

    This really should be the other way around.
     */

    /*
   3/21/23
   I am doing a major overhaul.  This is the plan:
   What order is required for initialization?

    1)initialize the artist first
    2)initialize the lists
    3)initialize the songsWithRatings

    [ArtistWithListsAndSongs(artist=Artist(name=Ear Kitty, artistId=1), artistLists=[SongListWithSongs(setList=SongList(listName=All Songs/Exercises, artistId=1, listId=1), songList=[SongWithRatings(song=Song(songTitle=Plush, artistName=Ear Kitty, bpm=72, songNotes=, artistSong=Ear KittyPlush), ratingHistory=[Rating(timeStamp=1682712248567, songTitle=Plush, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Dammit, artistName=Ear Kitty, bpm=218, songNotes=, artistSong=Ear KittyDammit), ratingHistory=[Rating(timeStamp=1682712249337, songTitle=Dammit, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Sympathy For The Devil, artistName=Ear Kitty, bpm=117, songNotes=, artistSong=Ear KittySympathy For The Devil), ratingHistory=[Rating(timeStamp=1682712249955, songTitle=Sympathy For The Devil, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Interstate Love Song, artistName=Ear Kitty, bpm=86, songNotes=, artistSong=Ear KittyInterstate Love Song), ratingHistory=[Rating(timeStamp=1682712250214, songTitle=Interstate Love Song, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Santeria, artistName=Ear Kitty, bpm=90, songNotes=, artistSong=Ear KittySanteria), ratingHistory=[Rating(timeStamp=1682712250902, songTitle=Santeria, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Fly Away, artistName=Ear Kitty, bpm=80, songNotes=, artistSong=Ear KittyFly Away), ratingHistory=[Rating(timeStamp=1682712251331, songTitle=Fly Away, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Say It Ain't So, artistName=Ear Kitty, bpm=76, songNotes=, artistSong=Ear KittySay It Ain't So), ratingHistory=[Rating(timeStamp=1682712251987, songTitle=Say It Ain't So, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=The Middle, artistName=Ear Kitty, bpm=162, songNotes=, artistSong=Ear KittyThe Middle), ratingHistory=[Rating(timeStamp=1682712252320, songTitle=The Middle, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Lithium, artistName=Ear Kitty, bpm=120, songNotes=, artistSong=Ear KittyLithium), ratingHistory=[Rating(timeStamp=1682712252749, songTitle=Lithium, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Three Little Birds, artistName=Ear Kitty, bpm=74, songNotes=, artistSong=Ear KittyThree Little Birds), ratingHistory=[Rating(timeStamp=1682712253243, songTitle=Three Little Birds, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Twist and Shout, artistName=Ear Kitty, bpm=124, songNotes=, artistSong=Ear KittyTwist and Shout), ratingHistory=[Rating(timeStamp=1682712253663, songTitle=Twist and Shout, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Californication, artistName=Ear Kitty, bpm=96, songNotes=, artistSong=Ear KittyCalifornication), ratingHistory=[Rating(timeStamp=1682712254286, songTitle=Californication, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=House Of The Rising Sun, artistName=Ear Kitty, bpm=77, songNotes=, artistSong=Ear KittyHouse Of The Rising Sun), ratingHistory=[Rating(timeStamp=1682712254571, songTitle=House Of The Rising Sun, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Welcome To Paradise, artistName=Ear Kitty, bpm=176, songNotes=, artistSong=Ear KittyWelcome To Paradise), ratingHistory=[Rating(timeStamp=1682712255286, songTitle=Welcome To Paradise, artistName=Ear Kitty, rating=50)], expand=false, isSelected=false), SongWithRatings(song=Song(songTitle=Headsp
D

    Do I need dummy/default values for initialization?

    Strategy 1 ***
    Load everything and filter

    start with allArtists --> currentArtistLive

    add allArtistsWithListsAndSongs  --> currentArtistLists: filtered by currentArtistLive

    add allListsWithSongs  --> currentListLive: filtered from currentArtistLists and
                                           from a selection stored in the viewmodel

    add allSongsWithRatings  --> currentSongsLive:the actual songs being displayed as filtered
                                              by currentListLive

     */
    //These 4 will be the big loads that will be filtered
    private val currentArtistWithListsAndSongs = repository.currentArtistWithListsAndSongs
    private val allSongsWithRatings = repository.allSongsWithRatings

    val allSongsWithRatingsLiveData = allSongsWithRatings.asLiveData() as LiveData<List<SongWithRatings>>

    val allArtistWithListsAndSongs : LiveData<List<ArtistWithListsAndSongs>> = repository.allArtistWithListsAndSongs.asLiveData()


    //end of primary load from database


    //start
    //default artist name for this phase of production
    private var defaultArtistName = "Ear Kitty"

    //start
    //default list for this phase of production
    val defaultList = SongList("All Songs/Exercises",1)
    val defaultArtist = Artist("Ear Kitty")

    val currentArtist: LiveData<Artist> =  currentArtistWithListsAndSongs.map {
        it.artist
    }.asLiveData()

    val currentArtistName : LiveData<String> = currentArtistWithListsAndSongs.map {
        it.artist.name ?: defaultArtistName
    }.asLiveData()

    val currentArtistLists : LiveData<List<SongListWithSongs>> = currentArtistWithListsAndSongs.map{
        it.artistLists
    }.asLiveData()

    //the master song list shown in main fragment
    var _mainListForFiltering : MutableLiveData<SongListWithSongs> = currentArtistWithListsAndSongs.map{
        it.artistLists.firstOrNull()
    }.asLiveData() as MutableLiveData<SongListWithSongs>

    val mainListForFiltering : LiveData<SongListWithSongs>
        get() = _mainListForFiltering

    val mainFragmentSongList =  MediatorLiveData<List<SongWithRatings>>().apply {
        addSource(mainListForFiltering) { list ->
            // When the current list changes, update the filtered list
            value = filterSongsByList(allSongsWithRatings.asLiveData().value, list)
        }
        addSource(allSongsWithRatings.asLiveData()) { songs ->
            // When the songs with ratings change, update the filtered list
            value = filterSongsByList(songs, mainListForFiltering.value)
        }
    }

    private fun filterSongsByList(songs: List<SongWithRatings>?, list: SongListWithSongs?): List<SongWithRatings> {
        return songs?.filter { song ->
            list?.songList?.contains(song.song) ?: false
        } ?: emptyList()
    }



    var selectedSongs = mutableListOf<SongWithRatings>()

    //this gets songs by Artist.name, may change to artistId

    ////////////

    private var temp = mutableListOf<SongWithRatings>()
    private val _practiceList = MutableLiveData<List<SongWithRatings>>()

    val practiceList: LiveData<List<SongWithRatings>>
        get() = _practiceList

    init {
        _practiceList.value = temp
        //currentArtist.artistId = 1
        viewModelScope.launch {
            repository.allArtistWithListsAndSongs.collect { artists ->
                Log.d("MyViewModel", "All artists with lists and songs: ${artists.drop(1)}")
            }
        }

    }


    //function used for passing a song to a fragment as an argument
    fun getArgumentSong(songTitle: String): SongWithRatings{
        val argSong = allSongsWithRatings.asLiveData().value?.find { songWithRatings: SongWithRatings ->
            songTitle == songWithRatings.song.songTitle

        }
        return argSong!!
    }
    fun getArtistNameList(onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val artists = withContext(Dispatchers.IO) { repository.getArtistNameList() }

            // maybe validate data, e.g. not empty

            onResult(artists)
        }
    }

    //may need to change to ID only works for existing artists
    fun changeArtist(artist: String){
        //this can change the artist if the artist is in the db
        repository.setCurrentArtistByName(artist)
    }
    //no good for new artists
    fun changeArtist(artistId: Long) = viewModelScope.launch {
        Log.d("Change Artist","changeArtist called")
        repository.setCurrentArtistById(artistId)
        //initializeWithArtist()

    }
    fun testListFunctionality() = viewModelScope.launch{
        val testList = withContext(Dispatchers.IO) {
        _mainListForFiltering.postValue(repository.getSongListWithRatings(5).asLiveData().value)
        Log.d("Test List Functionality called","mainListForFiltering is named " + _mainListForFiltering.value?.setList?.listName)}
    }


    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * these all interact with the repository
     */
    fun insertSong(newSongTitle: String, newSongBPM: Int) = viewModelScope.launch{
        if(TextUtils.isEmpty(newSongTitle)){

        }else{
            val songArtist = currentArtist.value?.name ?: "Unknown Artist"
            val songArtistId = currentArtist.value?.artistId ?: 0


            val newSong = if(newSongBPM > 1){
                Song(newSongTitle.trim(), songArtist, songArtistId, newSongBPM)
            }else{
                Song(newSongTitle.trim(), songArtist, songArtistId)
            }

            val newSongId = repository.insertSong(newSong)
            //includes default start rating, may change
            //this inserts the song, an initial rating and adds the song to "All Songs/Exercises" list
            val rating = Rating(System.currentTimeMillis(),newSong.songTitle, newSongId , 50)
            if (rating != null) {
                insertRating(rating)
            }
            //this inserts the song into the artists "All Songs/Exercises" list

            val listId = mainListForFiltering.value?.setList?.listId
            if (listId != null) {
                addSongToList(listId,
                    newSongId)
            }

        }
    }

    fun insertRating(rating: Rating) = viewModelScope.launch {
        repository.insertRating(rating)
    }

    //insertArtist will now also add a list for a master song list
    fun insertArtist(artist: Artist) = viewModelScope.launch {
        //this assumes that row ID is also artistId.  will test
        val artistId = repository.insertArtist(artist)
        Log.d("insertArtist"," artistId = " + artistId)
        val newMasterList = SongList("All Songs/Exercises",artistId)
        repository.insertList(newMasterList)
        changeArtist(artistId)

    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun deleteSong(songId: Long) = viewModelScope.launch {
        repository.deleteSong(songId)
    }

    //deletes selected songs within the scope of the current artist
    fun deleteSelectedSongs() = viewModelScope.launch {
        if(!mainFragmentSongList.value.isNullOrEmpty()){
            val selected: List<SongWithRatings> = mainFragmentSongList.value!!.filter { it.isSelected }
            //using artistSong for deletion 12/8/22
            selected.forEach { deleteSong(it.song.id) }
        }

    }

    fun updateSongNotes(oldSongWithRatings: SongWithRatings, notes: String) =
        viewModelScope.launch {

            val updatedSong = oldSongWithRatings.song.copy(songNotes = notes)
            repository.updateSong(updatedSong)
        }

    fun saveSelected(){
        selectedSongs = (mainFragmentSongList.value as MutableList<SongWithRatings>).filter { it.isSelected } as MutableList<SongWithRatings>
    }

    fun changeListByName(listName:String){
        /*
        val currentList = artistLists?.find { it.setList.listName == listName }
        Log.d("changeListByName called","currentList is " + currentList?.setList?.listName)
        currentListLive  = currentList?.setList?.listId?.let {
            repository.getSongListWithRatings(
                it
            ).asLiveData()
        } as MutableLiveData<SongListWithRatings>
        val itit = currentListSongsWithRatings.value?.songList?.iterator()
        if (itit != null) {
            while (itit.hasNext()){
                Log.d("changeListByName called","currentList is " + itit.next().song.songTitle)
            }
        }

         */
    }

    fun createNewList(newListName:String, artist: Artist)= viewModelScope.launch{
        //should I be doing a null check here?!?  something is very
        //wrong if currentArtist is null at this point

        val newList = SongList(newListName, artist.artistId)

        //returns the row of the list which happens to be the listId
        val listId = repository.insertList(newList)

        selectedSongs.forEach {
            val newListAssociation = SongListSongM2M(listId,it.song.id)
            repository.insertListAssociation(newListAssociation)
        }


    }

    //adds songs to an existing list
    fun addSongToList(listId: Long, songId:Long) = viewModelScope.launch{
        val newListAssociation = SongListSongM2M(listId,songId)
        repository.insertListAssociation(newListAssociation)
    }


    /*
    end repository functions
     */

    fun getSongTitles(): ArrayList<String>
    {
        val songTitles = arrayListOf<String>()

        val iterator = allSongsWithRatingsLiveData.value?.listIterator()

        if (iterator != null){
            while (iterator.hasNext())
                songTitles.add(iterator.next().song.songTitle)
        }

        return songTitles.distinct().toCollection(ArrayList<String>())

   }

    fun getListTitles(): ArrayList<String>
    {

        Log.d("getListTitles called","testing")
        val listTitles = arrayListOf<String>()

        if(currentArtistLists.value.isNullOrEmpty())
            Log.d("getListTitles called","allArtistLists.value.isNullOrEmpty")

        val iterator = currentArtistLists.value!!.listIterator()

        while (iterator.hasNext())
            listTitles.add(iterator.next().setList.listName)

        return listTitles.distinct().toCollection(ArrayList<String>())

    }

    fun getTempo(songTitle: String): Int{
        val song = allSongsWithRatings.asLiveData().value?.find { songWithRatings: SongWithRatings ->
            songTitle == songWithRatings.song.songTitle

        }
        return song?.song?.bpm ?: 0

    }

    fun initializeWithArtist() = viewModelScope.launch{

        //initialize artistSongs WithRatings
        Log.d("Initialize Artist","initializeWithArtist called")
        var newList = mutableListOf<SongWithRatings>()
        newList = allSongsWithRatingsLiveData.value as MutableList<SongWithRatings>


    }
    /*
    fun initializeAllArtistLists(){
        currentArtistAndLists = allArtistsWithLists.value?.find { it.artist.artistId == currentArtist.artistId }!!
        Log.d("initializeAllArtistLists"," size of allLists " + allListsWithRatings.value?.size.toString())
        Log.d("initializeAllArtistLists"," currentArtistLive.artistId is " + currentArtistLive.value?.artistId.toString())
        Log.d("initializeAllArtistLists","    allArtist")
        Log.d("initializeAllArtistLists","size of list is " + allArtistListsWithRatings.value?.size.toString())
        Log.d("initializeAllArtistLists","list owner is " + allArtistListsWithRatings.value?.get(0)?.setList?.artistId.toString())
    }

     */
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
        newList = mainFragmentSongList.value as MutableList<SongWithRatings>

        //make sure the list isn't empty
        if(newList.isNullOrEmpty()){
            //handle empty list error
        }else{
            //sort list by performance rating
            //update song ratings
            newList.forEach{
                it.recentPerformanceRating()
            }
            //list sorted by performance rating
            _practiceList.value = newList.sortedByDescending{ it.recentPerformanceRating() } as MutableList<SongWithRatings>
            practiceList.value

        }

    }
    fun sortByTimestamp(){
        var newList = mutableListOf<SongWithRatings>()
        newList = mainFragmentSongList.value as MutableList<SongWithRatings>

        //make sure the list isn't empty
        if(newList.isNullOrEmpty()){
            //handle empty list error
        }else{
            //sort list by performance rating
            //update song ratings
            newList.forEach{
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