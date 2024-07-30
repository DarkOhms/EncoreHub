package com.lukemartinrecords.encorehub.model

import android.util.Log
import androidx.lifecycle.*
import com.lukemartinrecords.encorehub.data.SongRepository
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class SongViewModel(private val repository: SongRepository) : ViewModel() {

    val allArtists: LiveData<List<Artist>> =  repository.allArtists

    private val _currentArtistLive = MutableLiveData<Artist>()
    val currentArtistLive: LiveData<Artist>
        get() = _currentArtistLive
    //end


    private val _allSongsWithRatings: MutableLiveData<List<SongWithRatings>> =
        repository.allSongsWithRatings.asLiveData() as MutableLiveData<List<SongWithRatings>>

    val allSongsWithRatings: LiveData<List<SongWithRatings>>
        get() = _allSongsWithRatings

    //this gets songs by Artist.name, may change to artistId
    val allArtistSongsWithRatings: LiveData<List<SongWithRatings>> = currentArtistLive.switchMap { artist ->
        if (artist == null) {
            // Handle null artist case (optional)
            return@switchMap liveData { emit(emptyList()) } // or return LiveData with empty list
        } else {
            allSongsWithRatings.map { allSongs ->
                allSongs.filter { it.song.artistId == artist.artistId }
            }
        }
    }

    var selectedSongs = mutableListOf<SongWithRatings>()

    //////////////
    private val _allListsWithRatings: MutableLiveData<List<SongListWithRatings>> =
        repository.allListWithRatings.asLiveData() as MutableLiveData<List<SongListWithRatings>>

    val allListsWithRatings: LiveData<List<SongListWithRatings>>
        get() = _allListsWithRatings

    val allArtistListsWithRatings: LiveData<List<SongListWithRatings>> = currentArtistLive.switchMap { artist ->
        if (artist == null) {
            Log.d("allArtistListsWithRatings","artist is null")
            // Handle null artist case (optional)
            return@switchMap liveData { emit(emptyList()) } // or return LiveData with empty list
        } else {
            Log.d("allArtistListsWithRatings","artist is not null")
            allListsWithRatings.map { allLists ->
                allLists.filter { it.setList.artistId == artist.artistId }
            }
        }
    }

    val songSuggestions: LiveData<List<Song>>
        get() = _songSuggestions

    val _songSuggestions: MutableLiveData<List<Song>> = MutableLiveData()

    ////////////

    //start
    //default list for this phase of production
    /*
    val defaultList = SongList("All Songs/Exercises",1)
    val defaultSongListWithRatings = listOf<SongWithRatings>()

    var  currentList: SongListWithRatings  = SongListWithRatings(defaultList,defaultSongListWithRatings)


     */
    val listFilter: MutableLiveData<Long> = MutableLiveData<Long>(1)



    val currentSetListLive: LiveData<SongListWithRatings> by lazy {
        listFilter.switchMap{ listId ->

        Log.d("currentSetListLive","mapping")
        Log.d("currentSetListLive",listId.toString())
        allListsWithRatings.map { list ->
            list.find { it.setList.listId == listId }!!
        }

        }
    }
    //initial sort function by performance rating

    val practiceSortByFunction: MutableLiveData<(List<SongWithRatings>) -> List<SongWithRatings>> = MutableLiveData{
        it.sortedByDescending { songWithRatings -> songWithRatings.recentPerformanceRating() }
    }

    val performSortByFunction: MutableLiveData<(List<SongWithRatings>) -> List<SongWithRatings>> = MutableLiveData{
        it.sortedByDescending { songWithRatings -> songWithRatings.recentPerformanceRating() }
    }

    val performFilterFunction: MutableLiveData<(List<SongWithRatings>) -> List<SongWithRatings>> = MutableLiveData{
        it.filter { songWithRatings -> (songWithRatings.recentPerformanceRating() > 77) }
    }

    val practiceListLive = MediatorLiveData<List<SongWithRatings>>().apply{

        addSource(practiceSortByFunction) {sortByFunction ->
            value = currentSetListLive.value?.songList?.let { songList ->
                sortByFunction?.invoke(songList)
            }
        }
        addSource(currentSetListLive) {list ->
            value = list.songList.let { songList -> practiceSortByFunction.value?.invoke(songList) }
        }
    }

    val performanceListLive = MediatorLiveData<List<SongWithRatings>>().apply{

        addSource(performSortByFunction) {sortByFunction ->
            value = currentSetListLive.value?.songList?.let { songList ->
                sortByFunction?.invoke(songList)
            }
        }
        addSource(performFilterFunction){filterFunction ->
            value = currentSetListLive.value?.songList?.let { songList ->
                val filtered = filterFunction?.invoke(songList)
                performSortByFunction.value?.invoke(filtered!!)
            }
        }
        addSource(currentSetListLive) {list ->
            value = list.songList.let { songList ->
                //filter then sort
                val filtered = performFilterFunction.value?.invoke(songList)
                performSortByFunction.value?.invoke(filtered!!)
            }
        }
    }

    //end



    init {
        //this initializes the currentArtistLive to the first artist in the db
        viewModelScope.launch {
            repository.allArtists.observeForever {
                if(it.isNotEmpty()){
                    _currentArtistLive.value = it.firstOrNull()
                    repository.allArtists.removeObserver{}
                }
            }
        }
    }


    fun getArgumentSong(songTitle: String): SongWithRatings{
        val argSong = allArtistSongsWithRatings.value?.find { songWithRatings: SongWithRatings ->
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

    //may need to change to ID
    fun changeArtist(artist: String){
        //this can change the artist if the artist is in the db
        allArtists.value?.find {
            it.name.toString() == artist
        }?.let { changeArtist(it) }
    }
    //no good for new artists
    fun changeArtist(artist: Artist) {
        Log.d("Change Artist","changeArtist called")
        _currentArtistLive.value = artist
        initializeWithArtist()

    }
    fun changeNewArtist(artist: Artist, artistId: Long) {
        Log.d("Change New Artist","changeNewArtist called")
        artist.artistId = artistId
        _currentArtistLive.value = artist

    }

    fun getSongListWithRatings(listId: Long):SongListWithRatings?{
        return allArtistListsWithRatings.value?.find { it.setList.listId == listId }
    }


    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * these all interact with the repository
     */
    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * these all interact with the repository
     */
    fun insertSong(song: Song) = viewModelScope.launch {
        val songId = repository.insertSong(song)
        //also adding the song to the master list
        val newListAssociation = SongListSongM2M(allArtistListsWithRatings.value?.get(0)!!.setList.listId,songId)
        repository.insertListAssociation(newListAssociation)
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
        val listId = repository.insertList(newMasterList)
        /*
            handling this in with initializeWithArtist was not working
            allArtistListsWithRatings.value would return null
            this is presumably because the list was not finished being inserted
            handling it here seems to ensure that the list is inserted before
            initializing the currentList could the same be done if initializeWithArtist
            were launched in the viewModelScope???
        */

        allArtistListsWithRatings.value?.find { it.setList.listId == listId }?.let { listFilter.value= it.setList.listId }
        changeNewArtist(artist,artistId)

    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun deleteSong(song:Song) = viewModelScope.launch {
        repository.deleteSong(song)
    }

    //deletes selected songs within the scope of the current artist
    fun deleteSelectedSongs() = viewModelScope.launch {
        if(!allArtistSongsWithRatings.value.isNullOrEmpty()){
            val selected: List<SongWithRatings> = allArtistSongsWithRatings.value!!.filter { it.isSelected }
            //using artistSong for deletion 12/8/22
            selected.forEach { deleteSong(it.song) }
        }

    }

    fun updateSongNotes(oldSongWithRatings: SongWithRatings, notes: String) =
        viewModelScope.launch {

            val updatedSong = oldSongWithRatings.song.copy(songNotes = notes)
            repository.updateSong(updatedSong)
        }

    fun saveSelected(){
        selectedSongs = (allArtistSongsWithRatings.value as MutableList<SongWithRatings>).filter { it.isSelected } as MutableList<SongWithRatings>
    }

    fun changeListByName(listName:String){
        Log.d("changeListByName", allArtistListsWithRatings.value.toString())
        //I will attempt to change by id instead of name this will require conversion
        val currentList = allArtistListsWithRatings.value?.find { it.setList.listName == listName }
        if(currentList != null) {
            listFilter.value = currentList.setList.listId
            Log.d("changeListByName", "currentList = " + listFilter.value!!)
        }else {
            Log.d("changeListByName", "currentList is null")
        }
    }

    fun createNewList(newListName:String, artist: Artist)= viewModelScope.launch{
        //should I be doing a null check here?!?  something is very
        //wrong if currentArtist is null at this point

        val newList = SongList(newListName, artist.artistId)

        //returns the row of the list which happens to be the listId
        val listId = repository.insertList(newList)

        selectedSongs.forEach {
            val newListAssociation = SongListSongM2M(listId,it.song.songId)
            repository.insertListAssociation(newListAssociation)
        }


    }

    fun getSongSuggestions(searchString: String) = viewModelScope.launch {
        val data = repository.getSongSuggestions(searchString)
        _songSuggestions.postValue(data)
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

    fun getListTitles(): ArrayList<String>
    {

        Log.d("getListTitles called","testing")
        val listTitles = arrayListOf<String>()

        if(allArtistListsWithRatings.value.isNullOrEmpty())
            Log.d("getListTitles called","allArtistListsWithRatings.value.isNullOrEmpty")

        val iterator = allArtistListsWithRatings.value!!.listIterator()

        while (iterator.hasNext())
            listTitles.add(iterator.next().setList.listName)

        return listTitles.distinct().toCollection(ArrayList<String>())

    }

    fun getTempo(songTitle: String): Int{
        val song = allSongsWithRatings.value?.find { songWithRatings: SongWithRatings ->
            songTitle == songWithRatings.song.songTitle

        }
        return song?.song?.bpm ?: 0

    }

    fun initializeWithArtist(){

        //initialize artistSongs WithRatings
        Log.d("Initialize Artist","currentArtistLive = " + currentArtistLive.value!!.name)

        if(allArtistListsWithRatings.value.isNullOrEmpty()){
            Log.d("Initialize Artist","allArtistListsWithRatings.value is currently null")
        }else {
            allArtistListsWithRatings.value?.get(0)!!.also { listFilter.value = it.setList.listId }
            Log.d("Initialize Artist", "currentList = " + listFilter.value.toString())
        }

    }
    /**
     * Business logic for creating lists
     * moved to SongViewModel from CreateSongList class
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

        //TODO modify the sorting and filters

    }
    fun sortByTimestamp(){

        Log.d("sortByTimestamp","called")
        practiceSortByFunction.value = {
            it.sortedByDescending { songWithRatings -> songWithRatings.lastPlayed()}
        }    //list sorted by performance rating

    }

    fun sortByPerformanceRating(){
        Log.d("sortByPerformanceRating","called")
        practiceSortByFunction.value = {
            it.sortedByDescending { songWithRatings -> songWithRatings.recentPerformanceRating() }
        }
    }

    fun setRatingFilterValue(newVal: Int) {
        performFilterFunction.value = {
            it.filter { songWithRatings -> (songWithRatings.recentPerformanceRating() > newVal) }
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