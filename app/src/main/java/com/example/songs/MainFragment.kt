package com.example.songs
/*
6/5/22
attempting to use view binding
 */
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.compose.ui.node.getOrAddAdapter
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songs.adapter.ItemAdapter
import com.example.songs.databinding.FragmentMainBinding
import com.example.songs.model.Rating
import com.example.songs.model.SongViewModel
import com.example.songs.model.SongViewModelFactory
import com.example.songs.model.SongWithRatings
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainFragment : Fragment(R.layout.fragment_main) {

    //lateinit var binding: FragmentMainBinding

    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels {SongViewModelFactory((requireActivity().application as SongApplication).repository)}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding = FragmentMainBinding.bind(view)

        //create view
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = ItemAdapter(requireActivity(), requireContext(),
            ItemAdapter.OnClickListener { id, song, newRating ->
                itemAdapterClick(id, song, newRating)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)


        //initialize data
        songViewModel.mainListForFiltering.observe(viewLifecycleOwner){

        }

        songViewModel.mainFragmentSongList.observe(viewLifecycleOwner) { songList ->
            // Update the cached copy of the songs in the adapter.
           Log.d("mainFragmentSongList","Main Fragment Observer called")
            Log.d("mainFragmentSongList", "current song list: $songList")
           Log.d("mainFragmentSongList", "Current artist name is " + songViewModel.currentArtist.value?.name)
            adapter.submitList(songList)

        }

        //add song button

        val fab =  view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            songViewModel.testListFunctionality()
            showNewSongDialog()
        }

    }

    private fun showNewSongDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = NewSongFragment()
        dialog.show(requireActivity().supportFragmentManager, "NewSongFragment")
    }

    private fun itemAdapterClick(id: Int, song: SongWithRatings, newRating: Int){
        //switch statement for different onClicks
        when(id){
            R.id.submitRating -> songViewModel.currentArtistName.value?.let {
                Rating(System.currentTimeMillis(),song.song.songTitle,
                    song.song.id, newRating )
            }?.let { songViewModel.insertRating(it) }
            //create rating fragment
            R.id.rateButton -> {
                Log.d("RatingButtonDebug","rating button clicked")
                var nextFrag1 = RatingFragment.newInstance(song)
                nextFrag1.show(requireActivity().supportFragmentManager, "SingleRatingFragment")
            }
            R.id.moreButton -> {
                var nextFrag = SongFragment.newInstance(song)
                Log.d("MoreButtonDebug","more button clicked")
                nextFrag.show(requireActivity().supportFragmentManager, "SingleSongFragment")

            }

        }
    }

}
