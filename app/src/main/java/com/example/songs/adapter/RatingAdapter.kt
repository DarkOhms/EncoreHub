package com.example.songs.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.songs.R
import com.example.songs.model.Rating
import com.example.songs.model.SongWithRatings

class RatingAdapter(private val context: Context) : ListAdapter<Rating, RatingAdapter.RatingViewHolder>(
    RatingAdapter.RatingComparator()
) {
    //copied for modification from ItemAdapter class
    class RatingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //time and date of rating
        val ratingDateTime: TextView = view.findViewById(R.id.ratingDateTime)

        //new rating code 8/29/2022
        val pastRatingBar: ProgressBar = view.findViewById(R.id.pastRatingBar)

        //
        val ratingLabel2: TextView = view.findViewById(R.id.ratingLabel2)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingAdapter.RatingViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.rating_item, parent, false)
        return RatingAdapter.RatingViewHolder(adapterLayout)

    }

    override fun onBindViewHolder(holder: RatingAdapter.RatingViewHolder, position: Int) {
        val item = getItem(position)
        val itemRating = item.rating


        holder.pastRatingBar.progress = item.rating
        holder.pastRatingBar.setBackgroundColor(getStatusColor(item.rating))
        holder.ratingLabel2.text = itemRating.toString()
        holder.ratingDateTime.text = item.ratingTimeString()


    }


    class RatingComparator : DiffUtil.ItemCallback<Rating>() {
        override fun areItemsTheSame(oldItem: Rating, newItem: Rating): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Rating, newItem: Rating): Boolean {
            return oldItem == newItem
        }
    }

    //8/28/2022 this is copied from the ItemAdapter class and will likely be modified
    class OnClickListener(val clickListener: (id: Int,song: SongWithRatings, newRating: Int) -> Unit) {
        fun onClick(id: Int, song: SongWithRatings,newRating: Int) = clickListener(id,song, newRating)
    }

    private fun getStatusColor(rating: Int): Int {
        val newColor: Int

        when (rating) {
            in 0..20 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color0)
            }
            in 21..40 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color20)
            }
            in 41..50 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color50)
            }
            in 51..55 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color55)
            }
            in 56..60 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color60)
            }
            in 61..65 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color65)
            }
            in 66..68 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color68)
            }
            in 69..70 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color70)
            }
            in 70..73 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color73)
            }
            in 74..76 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color76)
            }
            in 77..79 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color79)
            }
            80, 81 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color81)
            }
            82, 83 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color83)
            }
            84, 85 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color85)
            }
            86, 87 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color87)
            }
            88, 89 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color89)
            }
            90, 91 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color91)
            }
            92, 93 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color93)
            }
            94, 95 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color95)
            }
            96, 97 -> {
                newColor = ContextCompat.getColor(context, R.color.status_color97)
            }
            else -> {
                newColor = ContextCompat.getColor(context, R.color.status_color99)
            }
        }

        return newColor
    }
}