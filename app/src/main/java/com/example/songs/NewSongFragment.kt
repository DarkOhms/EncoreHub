package com.example.songs

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.songs.databinding.FragmentNewSongBinding
import com.example.songs.model.SongViewModel
import com.example.songs.model.SongViewModelFactory


/**
 * A simple [Fragment] subclass.
 * Use the [NewSongFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewSongFragment : DialogFragment() {
    lateinit var listener: NewSongListener

    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels { SongViewModelFactory((requireActivity().application as SongApplication).repository) }


    interface NewSongListener {
        fun onDialogPositiveClick(newSongTitle: String, newSongBPM: Int)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
   /*
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        return inflater.inflate(R.layout.fragment_new_song, container, false)

    }
*/
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NewSongListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NewSongListener"))
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

            //add inflater
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_new_song, null)

            //autoComplete
            val songTitles = songViewModel.getSongTitles()
            //this next line has to be here and not inside the onClick
            val tempoEditText = view?.findViewById<EditText>(R.id.newSongBpm)

            val arrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item, songTitles)
            val songAutoComplete = view?.findViewById<AutoCompleteTextView>(R.id.newSongTitle)
            songAutoComplete?.setAdapter(arrayAdapter)
            songAutoComplete?.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    //add the tempo
                    val songName = arrayAdapter.getItem(position)
                    if (songName != null) {
                        val songTempoSuggestion = songViewModel.getTempo(songName).toString()
                        tempoEditText?.setText(songTempoSuggestion)
                        Log.d("NewSongDebug", songName)
                    }

                }
            }
            builder
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel,DialogInterface.OnClickListener { dialog, id ->
                    dialog?.cancel()
                })
                .setPositiveButton(R.string.button_save)
                    {_, _ ->
                        Log.d("Applog", view.toString())
                        val newSongTitle = view?.findViewById<EditText>(R.id.newSongTitle)?.text
                        val newSongBPM = view?.findViewById<EditText>(R.id.newSongBpm)?.text
                        listener.onDialogPositiveClick(newSongTitle.toString(), newSongBPM.toString().toInt())
                    }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

}