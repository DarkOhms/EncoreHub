package com.lukemartinrecords.encorehub.api

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

fun parseSongJsonResultForBPM(jsonResult: JSONObject): ArrayList<Int> {
    val bpmArray = jsonResult.getJSONArray("tempo")
    val bpmList = ArrayList<Int>()
    for (i in 0 until bpmArray.length()) {
        bpmList.add(bpmArray.getInt(i))
    }
    return bpmList

}