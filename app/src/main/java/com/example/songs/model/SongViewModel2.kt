package com.example.songs.model

import androidx.lifecycle.*
import com.example.songs.data.SongRepository
import kotlinx.coroutines.flow.map

class SongViewModel2(private val repository: SongRepository) : ViewModel() {

    private val currentArtistWithListsAndSongs = repository.currentArtistWithListsAndSongs
    private val allSongsWithRatings = repository.allSongsWithRatings

    val currentArtistName : LiveData<String> = currentArtistWithListsAndSongs.map {
        it.artist.name ?: "Ear Kitty"
    }.asLiveData()

    val currentArtistLists : LiveData<List<SongListWithSongs>> = currentArtistWithListsAndSongs.map{
        it.artistLists
    }.asLiveData()

    val mainListForFiltering : LiveData<SongListWithSongs?> = currentArtistWithListsAndSongs.map{
        it.artistLists.firstOrNull()
    }.asLiveData()

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

}