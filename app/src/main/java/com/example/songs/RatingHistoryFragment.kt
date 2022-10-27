package com.example.songs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songs.adapter.RatingAdapter
import com.example.songs.databinding.FragmentRatingBinding
import com.example.songs.databinding.FragmentRatingHistoryBinding
import com.example.songs.model.SongViewModel
import com.example.songs.model.SongViewModelFactory
import com.example.songs.model.SongWithRatings
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * A simple [Fragment] subclass.
 * Use the [RatingHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RatingHistoryFragment : Fragment() {

    private lateinit var param1song: SongWithRatings
    lateinit var binding: FragmentRatingHistoryBinding
    private val safeArgs : RatingHistoryFragmentArgs by navArgs()


    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels { SongViewModelFactory((requireActivity().application as SongApplication).repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rating_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //create view
        param1song = songViewModel.getArgumentSong(safeArgs.song)

        binding = FragmentRatingHistoryBinding.bind(view)
        val header = binding.ratingHistoryHeader
        header.text = param1song.song.songTitle + " Rating History"
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        //different adapter from ItemAdapter  9/8/2022
        val adapter = RatingAdapter(requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        //initialize data
        //viewModel needed here??

        adapter.submitList(param1song.ratingHistory)

        val fab =  binding.fab
        fab.setOnClickListener {
            //TODO
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RatingHistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: SongWithRatings) =
            RatingHistoryFragment().apply {
                arguments = Bundle().apply {
                    param1song = param1
                }
            }
    }
}
