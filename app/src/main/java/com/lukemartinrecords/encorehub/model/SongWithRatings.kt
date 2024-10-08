package com.lukemartinrecords.encorehub.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

@Entity
data class SongWithRatings @JvmOverloads constructor(
    @Embedded val song: Song,
    @Relation(
        //dirty method using concatenated artist and song fields
        //because Room doesn't support a composite relation
        parentColumn = "songId",
        entityColumn = "songId",
    )
    val ratingHistory: List<Rating>,
    /*
    8/8/2022
    Attempting to fix the bug in the item adapter for expandability
     */
    @Ignore
    var expand: Boolean = false,
    //for checkbox selection
    @Ignore
    var isSelected: Boolean = false

){


fun recentPerformanceRating(): Int{
    /*  6/14/2024
    This now works properly for the possibility of zero ratings.
    Updates will be made to incorporate time intervals and a possible refactor
    to readiness rating.
     */
    //handle initial cases of 0 through 4
    when (ratingHistory.size) {
        //initial rating with no user input or practice sessions
        0 -> return 0
        //user rating with just one practice session
        1-> return ratingHistory[0].rating
        //only 2 practice sessions better weighting based on time intervals TO BE ADDED
        //current weighting is 60% for the most recent with 40% for the previous
        2 -> return ((ratingHistory[0].rating * .4) + (ratingHistory[1].rating *.6)).roundToInt()

        else -> {
            val i: Int = ratingHistory.size
            //with 3 or more practice sessions better weighting based on time intervals TO BE ADDED
            //current weighting is 66% for the most recent with 20% for the previous and 14% for the oldest
            return ((ratingHistory[i-3].rating * .14) + (ratingHistory[i-2].rating *.2) + (ratingHistory[i-1].rating *.66)).roundToInt()
        }
    }

}
    /*
    returns the value of the last time the song was
    practiced or performed for comparison purposes
     */
    fun lastPlayed():Duration {
        return if (ratingHistory.isEmpty()){
            Duration.ZERO
        }else{
            ratingHistory.last().timeStamp.nanoseconds
        }
    }

    fun lastPlayedString(): String{
        if (ratingHistory.isEmpty()){
            return "Never"
        }else{
            val raw : Long = ratingHistory.last().timeStamp
            val date = Date(raw)
            val dateFormat = SimpleDateFormat("MM/dd/yy hh:mm a",Locale.getDefault())
            return dateFormat.format(raw)
        }
    }
}
