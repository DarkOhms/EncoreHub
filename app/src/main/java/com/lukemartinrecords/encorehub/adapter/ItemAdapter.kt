package com.lukemartinrecords.encorehub.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lukemartinrecords.encorehub.MainActivity
import com.lukemartinrecords.encorehub.R
import com.lukemartinrecords.encorehub.model.Rating
import com.lukemartinrecords.encorehub.model.SongWithRatings

/**
 * 4/2/2022
 * The adapter has expandability through use of a visibility array although there seems
 * to be an ExpandableListAdapter class that I will want to implement for a cleaner
 * and mor
 */



class ItemAdapter(private val activity: Activity, private val context: Context, private val onClickListener: OnClickListener) : ListAdapter<SongWithRatings, ItemAdapter.SongViewHolder>(SongsComparator())
{
    //expandability has a bug, sometimes it works, other times it doesn't.
    //current debugging messages seem consistent with what is expected but actual expanding does not


    val thisAdapter: ItemAdapter = this
    lateinit var selectedItems: ArrayList<SongWithRatings>

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //added card view 9/30/22
        val cardView: CardView = view.findViewById(R.id.card_view)
        val textView: TextView = view.findViewById(R.id.item_title)
        val textViewBpm: TextView = view.findViewById(R.id.bpm)
        val lastPlayed: TextView = view.findViewById(R.id.lastPlayed)

        //new rating code 11/27/2021
        val ratingBar: SeekBar = view.findViewById(R.id.ratingBar)
        val ratingLabel: TextView = view.findViewById(R.id.ratingLabel)
        val button: Button = view.findViewById(R.id.submitRating)

        //expandable view 3/27/2022
        val titleView: LinearLayout = view.findViewById(R.id.titleView)
        val expand: ConstraintLayout = view.findViewById(R.id.expand)

        //fragment launch buttons
        val moreButton: Button = view.findViewById(R.id.moreButton)
        val rateButton: Button = view.findViewById(R.id.rateButton)

        //checkbox for selections
        val check: CheckBox = view.findViewById(R.id.checkBox)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return SongViewHolder(adapterLayout)

    }
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val item = getItem(position)
        //temporary code for initial rating
        val initialRating = item.recentPerformanceRating()
        var newRating = 0

        holder.textView.text = item.song.songTitle
        holder.textViewBpm.text = context.resources.getString(R.string.BPM,item.song.bpm)
        holder.ratingLabel.setBackgroundColor(getStatusColor(item.recentPerformanceRating()))
        //holder.imageView.setImageResource(item.imageResourceID)
        holder.ratingLabel.text = context.resources.getString(R.string.Rating, initialRating )
        holder.lastPlayed.text = item.lastPlayedString()

        //11/18/2022  this method just invokes the function in MainActivity rather than using a callback
        holder.titleView.setOnLongClickListener {
            Log.d("Applog", "LongClick in Adapter !  !  !  !" )
            //activate ActionMode or do nothing

            if(!(activity as MainActivity).isInActionMode){
                (activity as MainActivity).startActionMode(thisAdapter)
                notifyDataSetChanged()
                true
            }else {
                (activity as MainActivity).endActionMode()
            }

            true
        }

        //checkbox visibility per action mode
        if((activity as MainActivity).isInActionMode)
            holder.check.visibility = View.VISIBLE
        else
            holder.check.visibility = View.GONE

        //checkbox selection relies on the model isSelected property
        holder.check.setOnClickListener{
            if(!item.isSelected){
                item.isSelected = true
                (activity as MainActivity).selectionCounter +=1
            }else{
                item.isSelected = false
                (activity as MainActivity).selectionCounter -=1
            }
            (activity as MainActivity).updateCounter()
        }
        holder.check.isChecked = item.isSelected

        //rating bar functionality
        holder.ratingBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, rating: Int, fromUser: Boolean) {
                holder.ratingLabel.text  = context.resources.getString(R.string.Rating, rating)
                newRating = rating

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        //Performance Rating button functionality
        holder.button.setOnClickListener{

            onClickListener.onClick(it.id,item, newRating)
                //holder.textView.setBackgroundColor(getStatusColor(item.songRatingHistory.recentPerformanceRating()))
        }
        holder.moreButton.setOnClickListener {
            onClickListener.onClick(it.id,item, newRating)
        }
        holder.rateButton.setOnClickListener {
            onClickListener.onClick(it.id,item, newRating)
        }

        //expandability added 3/27/2022
        //val isExpandable: Boolean = isVisible[position]
        Log.d("Applog", "BeforeClick"+ position.toString() +" is " + item.expand.toString() )

        holder.titleView.setOnClickListener{
            item.expand = !(item.expand)
            notifyItemChanged(position)
            Log.d("Applog", "Click!!! "+ position.toString() +" is " + item.expand.toString() )
        }
        holder.expand.visibility = if(item.expand)  View.VISIBLE else View.GONE

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

    class SongsComparator : DiffUtil.ItemCallback<SongWithRatings>() {
        override fun areItemsTheSame(oldItem: SongWithRatings, newItem: SongWithRatings): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: SongWithRatings, newItem: SongWithRatings): Boolean {
            return oldItem.song.songTitle == newItem.song.songTitle
        }
    }

    class OnClickListener(val clickListener: (id: Int,song: SongWithRatings, newRating: Int) -> Unit) {
        fun onClick(id: Int, song: SongWithRatings,newRating: Int) = clickListener(id,song, newRating)
    }

}
