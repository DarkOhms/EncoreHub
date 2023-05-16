package com.example.songs.data

import android.content.Context
import androidx.annotation.StringRes
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.songs.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/*
12/30/2021
possible scope issues with fun populateDatabase
4/14/2022
beginning major conversion to version 3
12/13/2022
beginning conversion to version 4 for set list capabilities
4/3/23
Cleaning up song and using auto generate primary key so I can have
cascade delete instead of using artistSong as a dirty foreign key
method.  This requires a migration to version 6.
 */
@Database(version = 6, entities = [Song::class, Rating::class, Artist::class, Instrument::class,SongList::class, SongListSongM2M::class])
@TypeConverters(Converters::class)
abstract class SongRoomDatabase: RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun ratingDao(): RatingDao
    abstract fun artistDao(): ArtistDao
    abstract fun instrumentDao(): InstrumentDao
    abstract fun listDao(): ListDao
    abstract  fun listM2MDao():SongListSongM2MDao

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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {

                    //=================SONG TABLE MIGRATION====================
                    //save old table
                    execSQL("ALTER TABLE song_table RENAME TO song_table_old")
                    //create new table
                    execSQL("CREATE TABLE song_table" +
                            " (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " songTitle TEXT NOT NULL, artistName TEXT NOT NULL," +
                            " artistId INTEGER NOT NULL, songNotes TEXT NOT NULL)" +
                            " bpm INTEGER NOT NULL," +
                            " FOREIGN KEY (artistId) REFERENCES artist_table(artistId)" +
                            " ON DELETE CASCADE)")
                    //migrate good columns
                    execSQL("INSERT INTO song_table (songTitle,artistName, bpm, songNotes)" +
                            "SELECT songTitle, artistName, bpm, songNotes" +
                            "FROM song_table_old")
                    // Get a map of artist names to artist IDs
                    val artistIdMap = mutableMapOf<String, Long>()
                    database.query("SELECT name, artistId FROM artist_table").use { cursor ->
                        val nameIndex = cursor.getColumnIndex("name")
                        val artistIdIndex = cursor.getColumnIndex("artistId")
                        while (cursor.moveToNext()) {
                            val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                            val artistId = if (artistIdIndex >= 0) cursor.getLong(artistIdIndex) else null
                            name?.let { artistId?.let { id -> artistIdMap[name] = id } }
                        }
                    }


                    // Update the artistId column in the song_table based on the artistName values
                    artistIdMap.forEach { (name, artistId) ->
                        database.execSQL("UPDATE song_table SET artistId = $artistId WHERE artistName = '$name'")
                    }
                    execSQL("INSERT INTO song_table SELECT * FROM song_table_old")
                    //=================END MAIN SONG TABLE MIGRATION=============

                    //=================RATING TABLE MIGRATION====================
                    //save old table
                    execSQL("ALTER TABLE rating_table RENAME TO rating_table_old")
                    //create new table
                    execSQL("CREATE TABLE ratings_table" +
                            " (timeStamp INTEGER NOT NULL," +
                            " songTitle TEXT NOT NULL," +
                            " songId INTEGER NOT NULL," +
                            " rating INTEGER NOT NULL," +
                            " ratingId INTEGER PRIMARY KEY AUTOINCREMENT," +
                            " isQuickRate INTEGER NOT NULL DEFAULT 1," +
                            " ratingNotes TEXT NOT NULL DEFAULT '') " +
                            " FOREIGN KEY (songId) REFERENCES song_table(id) ON DELETE CASCADE)")

                    // Copy the data from the old table to the new table
                    // This is designed to add the new foreign key songId in place of artistSong
                    execSQL(
                        "INSERT INTO rating_table_new (song_id, rating) " +
                                "SELECT song_table.id, rating_table.rating " +
                                "FROM rating_table " +
                                "INNER JOIN song_table ON rating_table.artistSong = song_table.artist || ':' || song_table.song"
                    )

                    // Drop the old table


                    execSQL("DROP TABLE rating_table")


                    execSQL("INSERT INTO song_table SELECT * FROM song_table_old")
                    execSQL("DROP TABLE song_table_old")
                    //=================END SONG TABLE MIGRATION=============
                    //=================LIST TABLE MIGRATION=================
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS song_list_joining_table_new (" +
                                "listId INTEGER NOT NULL, " +
                                "songId INTEGER NOT NULL, " +
                                "FOREIGN KEY (listId) REFERENCES song_list_table (listId) ON DELETE CASCADE, " +
                                "FOREIGN KEY (songId) REFERENCES song_table (id) ON DELETE CASCADE, " +
                                "PRIMARY KEY (listId, songId))"
                    )

                    // Copy data from the old table to the new table
                    database.execSQL(
                        "INSERT INTO song_list_joining_table_new (listId, songId) " +
                                "SELECT listId, song_table.id " +
                                "FROM song_list_joining_table " +
                                "INNER JOIN song_table ON song_list_joining_table.artistSong = song_table.artist || ':' || song_table.song"
                    )

                    // Drop the old table
                    database.execSQL("DROP TABLE IF EXISTS song_list_joining_table")

                    // Rename the new table to the original table name
                    database.execSQL("ALTER TABLE song_list_joining_table_new RENAME TO song_list_joining_table")

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
                    populateDatabase(database.songDao(), database.ratingDao(), database.artistDao(),database.listDao(),database.listM2MDao())
                }

            }

        }

        /**
         * Populate the database in a new coroutine.
         * If you want to start with more songs, just add them.
         */

        suspend fun populateDatabase(songDao: SongDao,ratingDao: RatingDao, artistDao: ArtistDao, listDao: ListDao, listM2MDao:SongListSongM2MDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.

            //IMPORTANT!! default first artistId is 1
            val artistId = 1L

            val masterList = SongList("All Songs/Exercises",artistId)
            listDao.insert(masterList)

            //default values
            val gIr = 50
            val artistName = "Ear Kitty"
            var songId = 1L

            artistDao.insert(Artist(artistName))

            //create a song and an initial rating and associate it with the master list
            var song = Song("Plush", artistName,artistId, 72)
            songDao.insert(song)
            songId++

            var rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)

            var listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            //populate with more songs
            song = Song("Dammit", artistName,artistId,218 )
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle, songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Sympathy For The Devil", artistName,artistId,117)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle, songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Interstate Love Song", artistName,artistId,86)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle, songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Santeria", artistName,artistId, 90)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Fly Away", artistName,artistId, 80)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Say It Ain't So",artistName,artistId,  76)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("The Middle", artistName,artistId,162)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Lithium",artistName, artistId,120)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Three Little Birds",artistName,artistId, 74)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Twist and Shout",artistName,artistId, 124)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Californication", artistName,artistId,96)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("House Of The Rising Sun", artistName,artistId,77)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Welcome To Paradise", artistName,artistId,176)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Headspace", artistName,artistId,66)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Moves Like Jagger",artistName,artistId, 128)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Susie Q", artistName, artistId,130)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Hash Pipe", artistName, artistId,123)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Jumper", artistName, artistId,91)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Watermelon Sugar", artistName,artistId,  95)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Machinehead", artistName,artistId, 112)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Polly", artistName,artistId, 121)
            songDao.insert(song)
            songId++
            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Hound Dog", artistName,artistId, 87)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Highway to Hell", artistName,artistId, 117)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("No Rain", artistName,artistId, 152)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

            song = Song("Smells Like Teen Spirit", artistName, artistId,96)
            songDao.insert(song)
            songId++

            rating = Rating(System.currentTimeMillis(),song.songTitle,songId, gIr)
            ratingDao.insert(rating)
            listAssociation = SongListSongM2M(1,songId)
            listM2MDao.insert(listAssociation)

        }
    }

}

