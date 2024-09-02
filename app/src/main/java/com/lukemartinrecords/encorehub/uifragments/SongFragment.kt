package com.lukemartinrecords.encorehub.uifragments

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.lukemartinrecords.encorehub.EncoreHubApplication
import com.lukemartinrecords.encorehub.R
import com.lukemartinrecords.encorehub.databinding.FragmentSongBinding
import com.lukemartinrecords.encorehub.model.SongViewModel
import com.lukemartinrecords.encorehub.model.SongViewModelFactory
import com.lukemartinrecords.encorehub.model.SongWithRatings
import com.squareup.picasso.Picasso

/**
 * A simple [Fragment] subclass.
 * Use the [SongFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongFragment : DialogFragment(R.layout.fragment_song) {
    // TODO: Rename and change types of parameters
    private lateinit var param1song: SongWithRatings
    lateinit var binding: FragmentSongBinding
    private val albumArtURI = "https://ostrichtheory.com/wp-content/uploads/2020/02/20200206_150957-scaled.jpg"

    //shared view model for use in the fragment
    private val songViewModel: SongViewModel by activityViewModels { SongViewModelFactory((requireActivity().application as EncoreHubApplication).repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSongBinding.bind(view)
        binding.songTitleUI.text = param1song.song.songTitle
        binding.tempo.text = param1song.song.bpm.toString()


        val imgUri = albumArtURI.toUri().buildUpon().scheme("https").build()
       /* Picasso.get()
            .load(imgUri)
            .placeholder(R.drawable.album_placeholder)
            .into(binding.albumArt)

        */

        songViewModel.getAlbumArt(param1song.song)

        songViewModel.albumArtURI.observe(viewLifecycleOwner){
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.album_placeholder)
                .into(binding.albumArt)
        }



        //song notes text
        val editText: Editable = SpannableStringBuilder(param1song.song.songNotes)
        binding.editTextTextMultiLine.text = editText

        //click listeners
        binding.saveButton.setOnClickListener{
            //alert dialog to confirm save
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Are you sure you want to save?")
                    setIcon(R.drawable.ic_baseline_warning_24)
                    setPositiveButton(
                        R.string.button_save,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked saved button, implement save logic

                            //save notes logic
                            val newNotes = binding.editTextTextMultiLine.text.toString()
                            songViewModel.updateSongNotes(param1song,newNotes)


                        })
                    setNegativeButton(
                        R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                }

                // Create the AlertDialog
                builder.create()

            }
            alertDialog?.show()

        }

        binding.deleteButton.setOnClickListener {
            //alert dialog to confirm song delete
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Are you sure you want to delete?")
                    setIcon(R.drawable.ic_baseline_warning_24)
                    setPositiveButton(
                        R.string.confirm,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked saved button, implement delete logic
                            // Deletes the song and it's ratings and list associations
                            songViewModel.deleteSong(param1song.song)
                            getDialog()?.cancel()

                        })
                    setNegativeButton(
                        R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                            getDialog()?.cancel()
                        })
                }

                // Create the AlertDialog
                builder.create()

            }
            alertDialog?.show()
        }

        binding.floatingCancelButton.setOnClickListener { getDialog()?.cancel() }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SongFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: SongWithRatings) =
            SongFragment().apply {
                arguments = Bundle().apply {
                    param1song = param1
                }
            }
    }
}