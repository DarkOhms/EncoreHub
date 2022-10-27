package com.example.songs.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.songs.model.Artist
import com.example.songs.model.Instrument
import com.example.songs.model.Rating
import com.example.songs.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/*
12/30/2021
possible scope issues with fun populateDatabase
4/14/2022
beginning major conversion to version 3
 */
@Database(version = 3, entities = [Song::class, Rating::class, Artist::class, Instrument::class])
@TypeConverters(Converters::class)
abstract class SongRoomDatabase: RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun ratingDao(): RatingDao
    abstract fun artistDao(): ArtistDao
    abstract fun instrumentDao(): InstrumentDao

    companion object {
        @Volatile
        private var INSTANCE: SongRoomDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE song_table ADD COLUMN songNotes TEXT NOT NULL DEFAULT ''")
            }
        }

        //4/14/2022  big migration
        /**
         * 4/22/2022
         * migration notes and possible sql
         * UPDATE ratings_table SET artistName = 'Ear Kitty'
         * UPDATE ratings_table SET artistSong = 'Ear Kitty'  || songTitle
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    //add artist to song table with Ear Kitty default
                    execSQL("ALTER TABLE song_table ADD COLUMN artistName TEXT NOT NULL DEFAULT 'Ear Kitty'")
                    execSQL("ALTER TABLE song_table ADD COLUMN artistSong TEXT")
                    execSQL("UPDATE song_table SET artistSong = artistName || songTitle ")

                    /**
                     * recreate song_table to have a composite primary key
                     * https://stackoverflow.com/questions/52243349/how-to-alter-a-table-and-add-ondelete-cascade-constraint-sqlite#55078407
                     * https://sqlite.org/lang_altertable.html#otheralter
                     */
                    execSQL("ALTER TABLE song_table RENAME TO song_table_old")
                    execSQL("CREATE TABLE 'song_table' ('songTitle' TEXT NOT NULL," +
                            "'bpm' INTEGER NOT NULL," +
                            "'songNotes' TEXT NOT NULL DEFAULT ''," +
                            "'artistName' TEXT NOT NULL," +
                            "'artistSong' TEXT NOT NULL," +
                            "PRIMARY KEY('songTitle','artistName'))")
                    execSQL("INSERT INTO song_table SELECT * FROM song_table_old")
                    execSQL("DROP TABLE song_table_old")


                    //add artist to rating table with Ear Kitty default
                    execSQL("ALTER TABLE ratings_table ADD COLUMN artistName TEXT NOT NULL DEFAULT 'Ear Kitty'")
                    execSQL("ALTER TABLE ratings_table ADD COLUMN isQuickRate INTEGER NOT NULL DEFAULT 1 ")
                    execSQL("ALTER TABLE ratings_table ADD COLUMN artistSong TEXT ")
                    execSQL("ALTER TABLE ratings_table ADD COLUMN ratingNotes TEXT NOT NULL DEFAULT '' ")
                    execSQL("UPDATE ratings_table SET artistSong = artistName || songTitle ")


                    //add artist table
                    execSQL("CREATE TABLE 'artist_table' ('name' TEXT NOT NULL,'artistId' INTEGER PRIMARY KEY AUTOINCREMENT )")
                    //add instrument table
                    execSQL("CREATE TABLE 'instrument_table' ('artist' TEXT NOT NULL, 'instrument' TEXT NOT NULL," +
                            "'instrumentId' INTEGER PRIMARY KEY AUTOINCREMENT )")
                }
            }
        }


        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): SongRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SongRoomDatabase::class.java,
                    "song_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .addCallback(SongDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

    }



    private class SongDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.songDao(), database.ratingDao())
                }

            }

        }

        /**
         * Populate the database in a new coroutine.
         * If you want to start with more songs, just add them.
         */

        suspend fun populateDatabase(songDao: SongDao,ratingDao: RatingDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.

            val gIr = 50
            val artistName = "Ear Kitty"
            //create a song and an initial rating
            var song = Song("Plush", artistName,72)
            songDao.insert(song)
            var rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            //populate with more songs
            song = Song("Dammit", artistName,218)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle, artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Sympathy For The Devil", artistName,117)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle, artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Interstate Love Song", artistName,86)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle, artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Santeria", artistName, 90)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Fly Away", artistName, 80)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Say It Ain't So",artistName,  76)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("The Middle", artistName,162)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Lithium",artistName, 120)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Three Little Birds",artistName, 74)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Twist and Shout",artistName, 124)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Californication", artistName,96)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("House Of The Rising Sun", artistName,77)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Welcome To Paradise", artistName,176)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Headspace", artistName,66)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Moves Like Jagger",artistName, 128)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Susie Q", artistName,130)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Hash Pipe", artistName, 123)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Jumper", artistName, 91)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Watermelon Sugar", artistName,  95)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Machinehead", artistName, 112)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Polly", artistName, 121)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Hound Dog", artistName, 87)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Highway to Hell", artistName, 117)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("No Rain", artistName, 152)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)

            song = Song("Smells Like Teen Spirit", artistName, 96)
            songDao.insert(song)
            rating = Rating(System.currentTimeMillis(),song.songTitle,artistName, gIr)
            ratingDao.insert(rating)
        }
    }

}

