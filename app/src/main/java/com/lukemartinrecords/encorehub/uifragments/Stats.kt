package com.lukemartinrecords.encorehub.uifragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lukemartinrecords.encorehub.EncoreHubApplication
import com.lukemartinrecords.encorehub.R
import com.lukemartinrecords.encorehub.databinding.FragmentStatsBinding
import com.lukemartinrecords.encorehub.model.SongViewModel
import com.lukemartinrecords.encorehub.model.SongViewModelFactory

class Stats : Fragment() {

    private val songViewModel: SongViewModel by activityViewModels { SongViewModelFactory((requireActivity().application as EncoreHubApplication).repository) }
    lateinit var binding : FragmentStatsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStatsBinding.bind(view)

        songViewModel.currentArtistLive.observe(viewLifecycleOwner) {
                binding.playerNameTag.text = it.name
        }

        songViewModel.allArtistSongsWithRatings.observe(viewLifecycleOwner){
            val numberOfSongs = it.size
            val totalNumberOfRatings = songViewModel.getTotalNumberOfRatings()

            binding.songsExercisesCount.text = numberOfSongs.toString()
            binding.totalPracticesCount.text = totalNumberOfRatings.toString()
            Log.d("Stats", "stat bar size in dp: " + statBarSizeInDp(numberOfSongs).toString())
            binding.motionLayout.getConstraintSet(R.id.end).constrainWidth(R.id.bar1, statBarSizeInDp(numberOfSongs))
            binding.motionLayout.getConstraintSet(R.id.end).constrainWidth(R.id.bar2, statBarSizeInDp(totalNumberOfRatings))
            binding.motionLayout.transitionToEnd()
        }

    }

    fun convertToScreenSizePercentage(value: Int): Int {
        var convertedValue = value
        if (value == 0) {
            return 0
        }else if (value > 10) {
            convertedValue *= 10
        }
        val metrics = resources.displayMetrics
        val dp = value / (metrics.densityDpi / 160f)
        return dp.toInt()
    }

    fun determineStatBarScale(size: Int):Int{
        var scale = 10
        if(size > 10){
            scale = 20
            return determineScaleRecursive(size,scale)
        }
        return scale
    }

    private fun determineScaleRecursive(size: Int, scale: Int): Int {

        return if(size > scale){
            determineScaleRecursive(size,scale*2)
        }else{
            scale
        }
    }

    fun statBarSizeInDp(size: Int): Int {

        val scale = determineStatBarScale(size)
        val percentageOfScreen = size/scale.toFloat()
        val metrics = resources.displayMetrics
        val dp = percentageOfScreen * metrics.widthPixels
        Log.d("Stats", "percentage of screen: " + percentageOfScreen.toString() )
        Log.d("Stats", "width pixels: " + metrics.widthPixels.toString())
        Log.d("Stats", "density is: " + metrics.density.toString())
        return dp.toInt()

    }
}