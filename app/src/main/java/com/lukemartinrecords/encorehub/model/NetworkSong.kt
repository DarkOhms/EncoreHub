package com.lukemartinrecords.encorehub.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Simplified data class for Song
@JsonClass(generateAdapter = true)
data class NetworkSong(
    @Json(name = "title") val title: String,
    @Json(name = "tempo") val tempo: String,
    @Json(name = "artist") val artist: NetworkArtist
)

// Simplified data class for Artist
@JsonClass(generateAdapter = true)
data class NetworkArtist(
    @Json(name = "name") val name: String
)

// Wrapper class for Song object
@JsonClass(generateAdapter = true)
data class NetworkSongWrapper(
    @Json(name = "song") val song: NetworkSong
) {
    fun getSongBPM(): Int {
       return song.tempo.toInt()
    }
}