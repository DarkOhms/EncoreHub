package com.lukemartinrecords.encorehub.data

import androidx.room.TypeConverter
import java.sql.Timestamp

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long
             {
                 if(value != null) {
                     return value.time
                 }else {
                     return 0
                 }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.sql.Date?)
            : Long {
        return date?.time ?: 0
    }
}
