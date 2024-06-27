package com.lukemartinrecords.encorehub.uifragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.lukemartinrecords.encorehub.EncoreHubApplication
import com.lukemartinrecords.encorehub.R
import com.lukemartinrecords.encorehub.model.Artist
import com.lukemartinrecords.encorehub.model.SongViewModel
import com.lukemartinrecords.encorehub.model.SongViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [SetListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class SetListFragment : DialogFragment() {

    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels { SongViewModelFactory((requireActivity().application as EncoreHubApplication).repository) }
    //artist?
    private lateinit var param1Artist: Artist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           //param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

            //add inflater
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.fragment_set_list, null)
            builder
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    dialog?.cancel()
                })
                .setPositiveButton(
                    R.string.button_save, DialogInterface.OnClickListener
                {dialog, id ->
                    Log.d("Applog", view.toString())
                    val newSetListName = view?.findViewById<EditText>(R.id.newSetListName)?.text
                    songViewModel.createNewList(newSetListName.toString(), param1Artist)
                })

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SetListFragment.
         */
        @JvmStatic
        fun newInstance(param1: Artist) =
            SetListFragment().apply {
                arguments = Bundle().apply {
                    param1Artist = param1
                }
            }
    }
}