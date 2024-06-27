package com.lukemartinrecords.encorehub.uifragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lukemartinrecords.encorehub.EncoreHubApplication
import com.lukemartinrecords.encorehub.R
import com.lukemartinrecords.encorehub.adapter.ItemAdapter
import com.lukemartinrecords.encorehub.model.Rating
import com.lukemartinrecords.encorehub.model.SongViewModel
import com.lukemartinrecords.encorehub.model.SongViewModelFactory
import com.lukemartinrecords.encorehub.model.SongWithRatings

class MainFragment : Fragment(R.layout.fragment_main) {

    //lateinit var binding: FragmentMainBinding

    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels { SongViewModelFactory((requireActivity().application as EncoreHubApplication).repository) }

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

        songViewModel.allArtistSongsWithRatings.observe(viewLifecycleOwner) { song ->
            // Update the cached copy of the songs in the adapter.
            Log.d("LiveDataDebug", "Main Fragment Observer called")
            Log.d(
                "LiveDataDebug",
                "Current artist live is " + songViewModel.currentArtistLive.value?.name.toString()
            )
                song.let { adapter.submitList(it) }

        }

        //add song button

        val fab =  view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
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
            R.id.submitRating ->  songViewModel.insertRating(
                Rating(
                    System.currentTimeMillis(),
                    song.song.songId,
                    newRating
                )
            )
            //create rating fragment
            R.id.rateButton -> {
                Log.d("RatingButtonDebug", "rating button clicked")
                var nextFrag1 = RatingFragment.newInstance(song)
                nextFrag1.show(requireActivity().supportFragmentManager, "SingleRatingFragment")
            }
            R.id.moreButton -> {
                var nextFrag = SongFragment.newInstance(song)
                Log.d("MoreButtonDebug", "more button clicked")
                nextFrag.show(requireActivity().supportFragmentManager, "SingleSongFragment")

            }

        }
    }

}