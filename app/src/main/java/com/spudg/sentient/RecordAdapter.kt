package com.spudg.sentient

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter(private val context: Context, private val items: ArrayList<RecordModel>) :
        RecyclerView.Adapter<RecordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recordRowLayout = view.findViewById<LinearLayout>(R.id.record_row_layout)!!
        val recordInnerRow = view.findViewById<LinearLayout>(R.id.record_row)!!
        val scoreView = view.findViewById<TextView>(R.id.score_record_row)!!
        val timeView = view.findViewById<TextView>(R.id.at_time)!!
        val dateView = view.findViewById<TextView>(R.id.date)!!
        val noteView = view.findViewById<ImageView>(R.id.notes_btn)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.record_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val record = items[position]

        val sdfDate = SimpleDateFormat("EEEE dd MMMM yyyy")
        val sdfTime = SimpleDateFormat("HH:mm")
        val date = sdfDate.format(record.time.toLong())
        val time = sdfTime.format(record.time.toLong())
        holder.dateView.text = date
        holder.timeView.text = "at $time"

        holder.scoreView.text = record.score.toString()

        when (record.score) {
            in 0..9 -> {
                holder.scoreView.setTextColor(-65527)
            }
            in 10..39 -> {
                holder.scoreView.setTextColor(-25088)
            }
            in 40..69 -> {
                holder.scoreView.setTextColor(-16728577)
            }
            in 70..89 -> {
                holder.scoreView.setTextColor(-16711896)
            }
            in 90..100 -> {
                holder.scoreView.setTextColor(-6881025)
            }
        }

        if (context is MainActivity) {
            try {
                if (date == sdfDate.format(items[position - 1].time.toLong())) {
                    holder.dateView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.v("Records", e.message.toString())
            }
        }

        holder.noteView.setOnClickListener {}
        holder.recordInnerRow.setOnClickListener {}
        holder.recordInnerRow.setOnLongClickListener {true}

    }

    override fun getItemCount(): Int {
        return items.size
    }


}