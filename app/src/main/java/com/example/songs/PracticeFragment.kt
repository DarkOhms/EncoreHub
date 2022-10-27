package com.example.songs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songs.adapter.ItemAdapter
import com.example.songs.model.Rating
import com.example.songs.model.SongViewModel
import com.example.songs.model.SongViewModelFactory
import com.example.songs.model.SongWithRatings


class PracticeFragment : Fragment() {

    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels(){SongViewModelFactory((requireActivity().application as SongApplication).repository)}
    private var actionMode: PrimaryActionMode? = null

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
        val adapter = ItemAdapter(requireActivity(),requireContext(),
            ItemAdapter.OnClickListener { id, song, newRating ->
                itemAdapterClick(id, song, newRating)
            }
        ) {
            if (actionMode != null) {
                (requireActivity() as MainActivity).isInActionMode = false
            }
            requireActivity().invalidateOptionsMenu()
            (requireActivity() as MainActivity).isInActionMode = true
            //actionMode = PrimaryActionMode.startActionMode(mActionModeCallback)

        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        //initialize data
        //initialize data

        songViewModel.sortByTimestamp()

        //observe changes to allSongs to update practice list
        songViewModel.allArtistSongsWithRatings.observe(viewLifecycleOwner){
            songViewModel.sortByTimestamp()
        }
        songViewModel.practiceList.observe(viewLifecycleOwner) { song ->
            // Update the cached copy of the songs in the adapter.
            song.let { adapter.submitList(it) }
        }

    }

    private fun itemAdapterClick(id: Int, song: SongWithRatings, newRating: Int){
        //switch statement for different onClicks
        when(id){
            R.id.submitRating ->  songViewModel.insertRating( Rating(System.currentTimeMillis(),song.song.songTitle,songViewModel.artistName, newRating ))
            R.id.moreButton -> {
                var nextFrag = SongFragment.newInstance(song)
                nextFrag.show(requireActivity().supportFragmentManager, "SingleSongFragment")
            }
        }
    }
}