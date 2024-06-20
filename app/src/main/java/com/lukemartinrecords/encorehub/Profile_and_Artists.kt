package com.lukemartinrecords.encorehub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton




class Profile_and_Artists : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_and__artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab =  view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showNewArtistDialog()
        }

    }
    private fun showNewArtistDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = NewArtistFragment()
        dialog.show(requireActivity().supportFragmentManager, "NewArtistFragment")
    }

}