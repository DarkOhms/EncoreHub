package com.example.songs.data

import android.content.Context
import android.util.Log
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
                // 1. Create new tables
                database.execSQL(
                    """
                    CREATE TABLE new_song_table (
                        songId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        songTitle TEXT NOT NULL,
                        artistId INTEGER NOT NULL,
                        bpm INTEGER NOT NULL,
                        songNotes TEXT NOT NULL,
                        FOREIGN KEY(artistId) REFERENCES artist_table(artistId) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE new_ratings_table (
                        ratingId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timeStamp INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        rating INTEGER NOT NULL,
                        isQuickRate INTEGER NOT NULL DEFAULT 1,
                        ratingNotes TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(songId) REFERENCES new_song_table(songId) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE new_list_table (
                        listId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        listName TEXT NOT NULL,
                        artistId INTEGER NOT NULL,
                        FOREIGN KEY(artistId) REFERENCES artist_table(artistId) ON DELETE CASCADE,
                        UNIQUE (listName, artistId)
                    )
                """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE new_song_list_joining_table (
                        listId INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        PRIMARY KEY (listId, songId),
                        FOREIGN KEY(songId) REFERENCES new_song_table(songId) ON DELETE CASCADE,
                        FOREIGN KEY(listId) REFERENCES new_list_table(listId) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // 2. copy data from old tables to new tables (adjust column mappings as needed)

                database.execSQL(
                    """
                    INSERT INTO new_song_table (songTitle, artistId, bpm, songNotes)
                    SELECT songTitle, (SELECT artistId FROM artist_table WHERE artist_table.name = song_table.artistName), bpm, songNotes
                    FROM song_table
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO new_ratings_table (timeStamp, songId, rating, isQuickRate, ratingNotes)
                    SELECT timeStamp, (SELECT songId FROM new_song_table WHERE new_song_table.songTitle = ratings_table.songTitle AND new_song_table.artistId = (SELECT artistId FROM artist_table WHERE artist_table.name = ratings_table.artistName)), rating, isQuickRate, ratingNotes
                    FROM ratings_table
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO new_list_table (listName, artistId)
                    SELECT listName, artistId
                    FROM list_table
                    """.trimIndent()
                )
                /*
                6/6/2024
                Now I clean up the dirty artistSong column and replace it with songId.
                I tried a fancy insert statement but it didn't work.  I ended up creating
                a bridge table and testing everything on a copy in DB browser
                 */

                //create bridge table
                database.execSQL(
                    """
                   CREATE TABLE "new_song_bridge_table" (
                   	"songId"	INTEGER NOT NULL,
                   	"artistName"	TEXT,
                   	"artistSong"	TEXT,
                   	"songTitle"	TEXT,
                   	"artistId"	INTEGER,
                   	PRIMARY KEY("songId")
                   )
                    """.trimIndent()
                )

                //fill with relevant data
                database.execSQL(
                    """
                     INSERT INTO new_song_bridge_table (songId, songTitle, artistId)
                    SELECT songId, songTitle, artistId
                    FROM new_song_table
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    UPDATE new_song_bridge_table
                    SET artistName =  (SELECT name FROM artist_table WHERE artist_table.artistId = new_song_bridge_table.artistId)
                    """.trimIndent()
                )

                //now I can populate the new_song_list_joining_table
                database.execSQL(
                    """
                    INSERT INTO new_song_list_joining_table(listId, songId)
                    SELECT sljt.listId, nspt.songId
                    FROM song_list_joining_table AS sljt
                    INNER JOIN new_song_bridge_table AS nspt
                      ON sljt.artistSong = nspt.artistSong
                    """.trimIndent()
                )

                // 3. Drop old tables
                database.execSQL("DROP TABLE song_table")
                database.execSQL("DROP TABLE ratings_table")
                database.execSQL("DROP TABLE list_table")
                database.execSQL("DROP TABLE new_song_bridge_table")
                database.execSQL("DROP TABLE song_list_joining_table")

                // 4. Rename new tables to original names
                database.execSQL("ALTER TABLE new_song_table RENAME TO song_table")
                database.execSQL("ALTER TABLE new_ratings_table RENAME TO ratings_table")
                database.execSQL("ALTER TABLE new_list_table RENAME TO list_table")
                database.execSQL("ALTER TABLE new_song_list_joining_table RENAME TO song_list_joining_table")

                //5. Add indicies
                database.execSQL(
                    """
                        CREATE INDEX `index_list_table_artistId` ON `list_table` (`artistId`)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX `index_list_table_listName_artistId` ON `list_table` (`listName`, `artistId`)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX `index_ratings_table_songId` ON `ratings_table` (`songId`)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX `index_song_list_joining_table_listId` ON `song_list_joining_table` (`listId`)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX `index_song_list_joining_table_songId` ON `song_list_joining_table` (`songId`)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX `index_song_table_artistId` ON `song_table` (`artistId`)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE UNIQUE INDEX `index_song_table_songTitle_artistId` ON `song_table` (`songTitle`, `artistId`)
                    """.trimIndent()
                )
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_5_6)
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

            //IMPORTANT!! artist has to exist first because it is the foreign key at the top of other tables

            Log.d("populateDatabase","populateDatabase")
            val artistName = "Student"
            val artistId = artistDao.insert(Artist(artistName))

            val gIr = 50

            //create a song and an initial rating and associate it with the master list
            var song = Song("C scale", artistId,72)
            val songId = songDao.insert(song)

            var rating = Rating(System.currentTimeMillis(), songId, gIr)
            ratingDao.insert(rating)

            val masterList = SongList("All Songs/Exercises",artistId)
            val listId = listDao.insert(masterList)

            var listAssociation = SongListSongM2M(listId, songId)
            listM2MDao.insert(listAssociation)

        }
    }

}

