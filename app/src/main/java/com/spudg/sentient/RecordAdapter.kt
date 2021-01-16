package com.spudg.sentient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spudg.sentient.databinding.RecordRowBinding
import org.w3c.dom.Text
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class RecordAdapter(private val context: Context, private val items: ArrayList<RecordModel>) :
        RecyclerView.Adapter<RecordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recordRowLayout = view.findViewById<LinearLayout>(R.id.record_row_layout)!!
        val recordInnerRow = view.findViewById<LinearLayout>(R.id.row_line)!!
        val moodIconView = view.findViewById<ImageView>(R.id.mood_icon)!!
        val moodNameView = view.findViewById<TextView>(R.id.mood_name)!!
        val timeView = view.findViewById<TextView>(R.id.at_time)!!
        val noteView = view.findViewById<ImageView>(R.id.notes_btn)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.record_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]

        holder.moodIconView.setBackgroundResource(R.drawable.ic_launcher_foreground)
        holder.moodNameView.text = record.mood.toString()
        holder.timeView.text = record.time

        holder.noteView.setOnClickListener {}
        holder.recordInnerRow.setOnClickListener {}
        holder.recordInnerRow.setOnLongClickListener {true}

    }

    override fun getItemCount(): Int {
        return items.size
    }


}