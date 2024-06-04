package com.example.songs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.songs.model.SongViewModel
import com.example.songs.model.SongViewModelFactory


class PracticeOrPerform : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    private val songViewModel: SongViewModel by activityViewModels {SongViewModelFactory((requireActivity().application as SongApplication).repository)}

    lateinit var navController: NavController
    lateinit var spinnerDialog: Spinner
    lateinit var listNames: ArrayList<String>


    //should I use a CursorAdapter?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_practice_or__perform, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        //spinner code
        spinnerDialog = view.findViewById(R.id.spinner_dialog)

        songViewModel.allArtistListsWithRatings.observe(viewLifecycleOwner){
            listNames = songViewModel.getListTitles()
            val arrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item, listNames)
            spinnerDialog.onItemSelectedListener = this
            spinnerDialog.adapter = arrayAdapter
        }

        //end spinner code
        view.findViewById<Button>(R.id.practice_button).setOnClickListener(this)
        view.findViewById<Button>(R.id.perform_button).setOnClickListener(this)
    }

    //1/11/23 I will add a popup that filters to change the active practice or performance list
    override fun onClick(v: View?) {



        when(v!!.id){
            R.id.practice_button -> {

                navController!!.navigate(R.id.action_practice_or_Perform_to_practiceFragment)
            }
            R.id.perform_button -> navController!!.navigate(R.id.action_practice_or_Perform_to_performFragment)
        }

    }


    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {
        Log.d("SpinnerDebug","You selected " + listNames[index] )
        songViewModel.changeListByName(listNames[index])

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.d("SpinnerDebug","You selected nothing")
    }

}