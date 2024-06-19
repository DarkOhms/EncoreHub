package com.example.songs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songs.adapter.ItemAdapter
import com.example.songs.model.Rating
import com.example.songs.model.SongViewModel
import com.example.songs.model.SongViewModelFactory
import com.example.songs.model.SongWithRatings


class PerformFragment : Fragment() {
    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels(){ SongViewModelFactory((requireActivity().application as SongApplication).repository) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perform, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inflate the layout for this fragment
        val recyclerView = view.findViewById<RecyclerView>(R.id.perform_recycler_view)
        val adapter = ItemAdapter(requireActivity(),requireContext(),
            ItemAdapter.OnClickListener { id, song, newRating ->
                itemAdapterClick(id, song, newRating)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        numberPicker.minValue = 65
        numberPicker.maxValue = 100

        numberPicker.setOnValueChangedListener { _, _, newVal ->
            songViewModel.setRatingFilterValue(newVal)
        }

        //observe changes to allSongs to update practice list
        songViewModel.allArtistSongsWithRatings.observe(viewLifecycleOwner){

        }

        songViewModel.currentListLive.observe(viewLifecycleOwner) { currentListLive ->
            Log.d("PracticeFragment LiveData","Current list live is updated")
            Log.d("PracticeFragment LiveData",currentListLive.setList.listName)

        }
        songViewModel.currentSetListLive.observe(viewLifecycleOwner){songList ->
            Log.d("LiveDataDebug","currentSetListLive observer called in PracticeFragment")
            songList?.setList?.listName?.let { Log.d("currentListDebug", it) }

        }

        songViewModel.performanceListLive.observe(viewLifecycleOwner) { song ->
            song.let { adapter.submitList(it) }
        }

    }

    private fun itemAdapterClick(id: Int, song: SongWithRatings, newRating: Int){
        //switch statement for different onClicks
        when(id){
            R.id.submitRating ->  songViewModel.insertRating( Rating(System.currentTimeMillis(),song.song.songId, newRating ))
            R.id.moreButton -> {
                var nextFrag = SongFragment.newInstance(song)
                nextFrag.show(requireActivity().supportFragmentManager, "SingleSongFragment")
            }
        }
    }

}