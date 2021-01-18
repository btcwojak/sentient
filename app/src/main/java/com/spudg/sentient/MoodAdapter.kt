package com.spudg.sentient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.mood_row.view.*

class MoodAdapter(private val context: Context, private val items: ArrayList<MoodModel>) :
        RecyclerView.Adapter<MoodAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val moodItem = view.mood_row_inner_layout!!
        val nameView = view.mood_name!!
        val colourView = view.mood_colour!!
        val updateView = view.update_mood!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.mood_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mood = items[position]
        holder.nameView.text = mood.name
        holder.colourView.setBackgroundColor(mood.colour)
        holder.updateView.setOnClickListener {
            if (context is MoodActivity) {
                context.updateMood(mood)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}