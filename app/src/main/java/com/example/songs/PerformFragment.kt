package com.example.songs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.songs.model.SongViewModel
import com.example.songs.model.SongViewModelFactory


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

}