package com.example.songs
/*
6/5/2020  3:11 PM
I'm attempting to convert to View Binding before adding multi-button functionality
using Kotlin lambda callbacks from ItemAdapter.

It doesn't seem to work.  First complication I noticed was the navigation and the action bar.
Second complication is that I'm not seeing any MainActivityBinding or activity_mainBinding

I'll check this out later:
https://www.youtube.com/watch?v=KwW99ihfUS0&ab_channel=GreyDevelopers
 */

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.songs.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(),NewSongFragment.NewSongListener, NewArtistFragment.NewArtistListener{


    private val songViewModel: SongViewModel by viewModels {
        SongViewModelFactory((application as SongApplication).repository)
    }
    var isInActionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //create splash screen
        installSplashScreen().apply {
            //setKeepVisibleCondition(songViewModel.allSongs.asFlow().asLiveData().value.isNullOrEmpty())

        }
        setContentView(R.layout.activity_main)
        //setup toolbar
        setSupportActionBar(findViewById(R.id.my_toolbar))

        songViewModel.artistNameLive.observe(this) { artistNameLive ->
            // Update the cached copy of the songs in the adapter.
            artistNameLive.let {supportActionBar?.subtitle = it  }
        }
        //supportActionBar?.subtitle = songViewModel.artistName
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.mainFragment, R.id.practice_or_Perform, R.id.profile_and_Artists, R.id.stats, R.id.ratingHistoryFragment))


        //setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostController.navController
        bottomNavigationView.setupWithNavController(navController)

        findViewById<Toolbar>(R.id.my_toolbar).setupWithNavController(navController,appBarConfiguration)
        setupActionBarWithNavController(navController,appBarConfiguration)

        //initialize viewModel
        songViewModel.allSongsWithRatings.observe(this){
            songViewModel.initializeWithArtist()
        }


    }
    //setting menu in action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.my_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_delete_songs -> {
            // User chose delete songs action
            val alertDialog: AlertDialog? = this.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton(R.string.delete,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked OK button
                            songViewModel.deleteAll()
                        })
                    setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                            dialog.cancel()
                        })
                }
                // Create the AlertDialog
                builder.create()
            }
            alertDialog?.show()
            true
        }

        R.id.action_choose_artist -> {
            //create artist list
            songViewModel.getArtistList { testList ->
                val artistList = testList.toTypedArray()


                // User chose choose artist action
                val alertDialog: AlertDialog? = this.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setTitle(R.string.choose_artist)
                        setItems(artistList, DialogInterface.OnClickListener { dialog, which ->
                            // The 'which' argument contains the index position
                            // of the selected item
                            songViewModel.changeArtist(artistList[which])
                            //val navHostController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                            //val mainFrag: MainFragment = navHostController?.childFragmentManager?.findFragmentById(R.id.mainFragment) as MainFragment
                            //mainFrag.notifyThisFragment()
                        })
                    }
                    // Create the AlertDialog
                    builder.create()
                }
                alertDialog?.show()
            }
            true
        }

        R.id.action_nuke_database -> {
            // User chose new list option
            val alertDialog: AlertDialog? = this.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton(R.string.delete,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked OK button
                            //(application as SongApplication).nukeDatabase()
                        })
                    setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                            dialog.cancel()
                        })
                }
                // Create the AlertDialog
                builder.create()
            }
            alertDialog?.show()
            true
        }
        R.id.action_make_list -> {
            // User chose delete songs action
            val alertDialog: AlertDialog? = this.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Sort practice list by freshness or by current rating.")
                    setPositiveButton(R.string.ratingSort,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked OK button
                            songViewModel.createPracticeList()
                        })
                    setNeutralButton(R.string.freshnessSort,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked freshness button
                            songViewModel.sortByTimestamp()
                        })
                    setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                            dialog.cancel()
                        })
                }

                // Create the AlertDialog
                builder.create()
            }
            alertDialog?.show()
            true

        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDialogPositiveClick(newSongTitle: String, newSongBPM: Int) {
        // User touched the dialog's positive button
        //6/15/2022 I may move much of this into the songViewModel at some point

        if(TextUtils.isEmpty(newSongTitle)){

        }else{
            val newSong =
                songViewModel.artistNameLive.value?.let {
                    Song(newSongTitle.trim(),
                        it, newSongBPM)
                }
            if (newSong != null) {
                songViewModel.insertSong(newSong)
                //includes default start rating, may change
                val rating = songViewModel.artistNameLive.value?.let {
                    Rating(System.currentTimeMillis(),newSong.songTitle,
                        it, 50)
                }
                if (rating != null) {
                    songViewModel.insertRating(rating)
                }
            }
        }

    }

    override fun onDialogPositiveClick(newArtist: String) {
        // User touched the dialog's positive button

        if(TextUtils.isEmpty(newArtist)){

        }else{
            val  tempArtist = Artist(newArtist.trim())
            songViewModel.insertArtist(tempArtist)
            songViewModel.changeArtist(newArtist.trim())
        }

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
    }


}
