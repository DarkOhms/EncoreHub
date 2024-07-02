package com.lukemartinrecords.encorehub.uifragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lukemartinrecords.encorehub.EncoreHubApplication
import com.lukemartinrecords.encorehub.R
import com.lukemartinrecords.encorehub.adapter.ItemAdapter
import com.lukemartinrecords.encorehub.model.Rating
import com.lukemartinrecords.encorehub.model.SongViewModel
import com.lukemartinrecords.encorehub.model.SongViewModelFactory
import com.lukemartinrecords.encorehub.model.SongWithRatings

class PracticeFragment : Fragment() {

    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels(){ SongViewModelFactory((requireActivity().application as EncoreHubApplication).repository) }
    val TAG = "PracticeFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_practice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inflate the layout for this fragment
        val recyclerView = view.findViewById<RecyclerView>(R.id.practice_recycler_view)
        val adapter = ItemAdapter(requireActivity(), requireContext(),
            ItemAdapter.OnClickListener { id, song, newRating ->
                itemAdapterClick(id, song, newRating)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)


        //observe changes to allSongs to update practice list
        songViewModel.allArtistSongsWithRatings.observe(viewLifecycleOwner){
            //songViewModel.sortByTimestamp()
        }

        songViewModel.currentSetListLive.observe(viewLifecycleOwner){songList ->
            Log.d(TAG, "currentSetListLive observer called in PracticeFragment")
            songList?.setList?.listName?.let { Log.d(TAG, it) }
            if(songList == null){
                Log.d(TAG, "songList is null")
            }else{
                Log.d(TAG, "songList is not null")
            }

        }

        songViewModel.practiceListLive.observe(viewLifecycleOwner) { song ->
            song.let { adapter.submitList(it) }
        }

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
            R.id.moreButton -> {
                var nextFrag = SongFragment.newInstance(song)
                nextFrag.show(requireActivity().supportFragmentManager, "SingleSongFragment")
            }
        }
    }
}