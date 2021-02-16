package com.spudg.sentient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.sentient.databinding.NoteRowBinding
import java.util.*

class NoteListAdapter(private val items: ArrayList<RecordModel>) :
        RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(val binding: NoteRowBinding) : RecyclerView.ViewHolder(binding.root)

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

            binding.noteBody.text = record.note
            binding.noteDate.text = binding.noteDate.context.getString(R.string.visualiser_note_from_date_time, day.toString(), Globals.getShortMonth(month), year.toString(), String.format("%02d", hour), String.format("%02d", minute))

        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

}