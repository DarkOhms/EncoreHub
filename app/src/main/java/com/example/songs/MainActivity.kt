package com.example.songs
/*
6/5/2020  3:11 PM
I'm attempting to convert to View Binding before adding multi-button functionality
using Kotlin lambda callbacks from ItemAdapter.

It doesn't seem to work.  First complication I noticed was the navigation and the action bar.
Second complication is that I'm not seeing any MainActivityBinding or activity_mainBinding

I'll check this out later:
https://www.youtube.com/watch?v=KwW99ihfUS0&ab_channel=GreyDevelopers

12/12/2022

Now that I have ActionMode working it's time for another DB upgrade to handle
the many to many relationship of lists to songs.
 */

import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.songs.adapter.ItemAdapter
import com.example.songs.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(),NewSongFragment.NewSongListener, NewArtistFragment.NewArtistListener{


    private val songViewModel: SongViewModel by viewModels {
        SongViewModelFactory((application as SongApplication).repository)
    }

    lateinit var allArtists: List<Artist>
    lateinit var currentArtist: Artist
    lateinit var artistListTitles: ArrayList<String>

    //actionMode variables and callback 11/29/22
    private var mActionMode: ActionMode? = null
    var isInActionMode = false
    var actionAdapter: ItemAdapter? = null
    var selectionCounter = 0

    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {


        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            mode.menuInflater.inflate(R.menu.menu_action_mode, menu)
            mode.title = "Select songs"
            isInActionMode = true
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_make_set_list -> {
                    //make a list
                    songViewModel.saveSelected()
                    showSetListDialog(currentArtist)
                    Toast.makeText(this@MainActivity, "Make Set List Selected", Toast.LENGTH_SHORT)
                        .show()
                    mode.finish()
                    true
                }
                R.id.action_delete_songs -> {
                    songViewModel.deleteSelectedSongs()
                    Toast.makeText(this@MainActivity, "Delete Songs selected", Toast.LENGTH_SHORT)
                        .show()
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            mActionMode = null
            isInActionMode = false
            selectionCounter = 0
            actionAdapter?.notifyDataSetChanged()
            //reset the isSelected field for the current list, is there a more efficient way???
            actionAdapter?.currentList?.forEach { it.isSelected = false }

        }
    }

    //method to end ActionMode from long click in adapter
    fun endActionMode(){
        mActionMode?.finish()
    }

    fun updateCounter(){
        mActionMode?.title = selectionCounter.toString() + " selected"
    }

    ////////////////////////////////////////////////////////////////
    //val counterTextView: TextView = findViewById(R.id.counter_text)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //create splash screen
        installSplashScreen().apply {
            //this.setKeepOnScreenCondition(SplashScreen.KeepOnScreenCondition { songViewModel.mainListForFiltering.value?.songList.isNullOrEmpty() })

        }
        setContentView(R.layout.activity_main)
        //setup toolbar
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //initialize viewModel

        songViewModel.allSongsWithRatingsLiveData.observe(this){

        }

        songViewModel.allArtistWithListsAndSongs.observe(this){
            Log.d("AllArtistsWithListsAndSongs", it.size.toString())
            Log.d("AllArtistsWithListsAndSongs", it.firstOrNull()?.artistLists?.firstOrNull()?.songList?.size.toString())
        }

        songViewModel.currentArtist.observe(this){
            supportActionBar?.subtitle = it.name
            currentArtist = it
        }


        val appBarConfiguration = AppBarConfiguration(setOf(R.id.mainFragment, R.id.practice_or_Perform, R.id.profile_and_Artists, R.id.stats, R.id.ratingHistoryFragment))


        //setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostController.navController
        bottomNavigationView.setupWithNavController(navController)

        findViewById<Toolbar>(R.id.my_toolbar).setupWithNavController(navController,appBarConfiguration)
        setupActionBarWithNavController(navController,appBarConfiguration)


    }
    fun startActionMode(adapter: ItemAdapter) {

        actionAdapter = adapter
        mActionMode = super.startActionMode(mActionModeCallback)
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
            val alertDialog: AlertDialog = this.let {
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
            alertDialog.show()
            true
        }

        R.id.action_choose_artist -> {
            //create artist list
            songViewModel.getArtistNameList { testList ->
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
        //5/16/2023  moved to songViewModel to add state data re artist
        songViewModel.insertSong(newSongTitle,newSongBPM)

    }

    override fun onDialogPositiveClick(newArtist: String) {
        // User touched the dialog's positive button

        if(TextUtils.isEmpty(newArtist)){

        }else{
            val  tempArtist = Artist(newArtist.trim())
            songViewModel.insertArtist(tempArtist)
        }

    }



    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
        dialog.dismiss()
    }

    private fun showSetListDialog(artist: Artist) {
        // Create an instance of the dialog fragment and show it
        Log.d("Set List Debug","Artist is " + artist.name + " as of MainActivity call")
        val dialog = SetListFragment.newInstance(artist)
        dialog.show(supportFragmentManager, "SetListFragment")
    }

}
