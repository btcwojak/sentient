package com.spudg.sentient

import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.sentient.databinding.NoteRowBinding
import com.spudg.sentient.databinding.RecordRowBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteListAdapter(private val context: Context, private val items: ArrayList<RecordModel>) :
        RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(val binding: NoteRowBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteRowBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        with(holder) {
            val record = items[position]

            val cal = Calendar.getInstance()
            cal.timeInMillis = record.time.toLong()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            if (record.note.isNotEmpty() && month == Calendar.getInstance().get(Calendar.MONTH) + 1) {
                binding.noteBody.text = record.note
                binding.noteDate.text = "from $day ${getShortMonth(month)} $year at $hour:$minute"
            } else {
                binding.noteMainRowLayout.visibility = View.GONE
                binding.noteRowLayout.visibility = View.GONE
                binding.noteRow.visibility = View.GONE
                binding.noteBody.visibility = View.GONE
                binding.noteDate.visibility = View.GONE // FIX WHITESPACE ISSUE
            }

        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun getShortMonth(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "Error"
        }
    }
}