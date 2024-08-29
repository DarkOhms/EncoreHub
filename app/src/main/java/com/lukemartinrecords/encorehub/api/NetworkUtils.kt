package com.lukemartinrecords.encorehub.api

import android.util.Log
import com.lukemartinrecords.encorehub.model.Song
import org.json.JSONObject

fun parseSearchJsonResult(jsonResult: JSONObject): ArrayList<Song> {
    val searchArray = jsonResult.getJSONArray("search")
    val songs = ArrayList<Song>()

    //right now were just parsing the title and BPM
    for (i in 0 until searchArray.length()) {
        val songObject = searchArray.getJSONObject(i)
        val song = Song(songObject.getString("title"), 1L, -1)
        songs.add(song)
    }
    return songs
}

fun parseSearchJsonResultForSongId(jsonResult: JSONObject): String {
    val searchArray = jsonResult.getJSONArray("search")
    val searchObject = searchArray.getJSONObject(0)
    return searchObject.getString("id")
}

fun parseSongJsonResultForBPM(resultObject: JSONObject): Int {

    Log.d("parseSongJsonResultForBPM", "Song Object: $resultObject")

    val songObject = resultObject.getJSONObject("song")
    val tempoString = songObject.getString("tempo")
    val tempo = tempoString.toInt()

    Log.d("parseSongJsonResultForBPM", "Tempo: $tempo")

    // Handle cases where tempo is missing or invalid
    if (tempo <= 0) {
        // Log a warning or throw an exception as needed
        Log.w("parseSongJsonResultForBPM", "Invalid tempo value: $tempo")
    }

    return tempo
}