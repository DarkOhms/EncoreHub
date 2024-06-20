package com.lukemartinrecords.encorehub

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class NewArtistFragment: DialogFragment() {
    lateinit var listener: NewArtistListener

    interface NewArtistListener {
        fun onDialogPositiveClick(newArtistName: String)
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
            listener = context as NewArtistListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NewSongListener"))
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

            //add inflater
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.fragment_new_artist, null)
            builder
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    dialog?.cancel()
                })
                .setPositiveButton(R.string.button_save)
                {_, _ ->
                    Log.d("Applog", view.toString())
                    val newArtistName = view?.findViewById<EditText>(R.id.newArtistName)?.text
                    listener.onDialogPositiveClick(newArtistName.toString())
                }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
}