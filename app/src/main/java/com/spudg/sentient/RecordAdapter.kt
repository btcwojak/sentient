package com.spudg.sentient

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.sentient.databinding.RecordRowBinding
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter(private val context: Context, private val items: ArrayList<RecordModel>) :
        RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(val binding: RecordRowBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = RecordRowBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }


    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {

        with(holder) {
            val record = items[position]

            val sdfDate = SimpleDateFormat("EEEE dd MMMM yyyy")
            val sdfTime = SimpleDateFormat("HH:mm")
            val date = sdfDate.format(record.time.toLong())
            val time = sdfTime.format(record.time.toLong())
            binding.date.text = date
            binding.atTime.text = "at $time"

            binding.scoreRecordRow.text = record.score.toString()

            when (record.score) {
                in 0..9 -> {
                    binding.scoreRecordRow.setTextColor(-65527)
                }
                in 10..39 -> {
                    binding.scoreRecordRow.setTextColor(-25088)
                }
                in 40..69 -> {
                    binding.scoreRecordRow.setTextColor(-16728577)
                }
                in 70..89 -> {
                    binding.scoreRecordRow.setTextColor(-16711896)
                }
                in 90..100 -> {
                    binding.scoreRecordRow.setTextColor(-6881025)
                }
            }

            if (context is MainActivity) {
                try {
                    if (context.newerRecordOnDay(record)) {
                        binding.date.visibility = View.GONE
                    } else {
                        binding.date.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.v("RecordAdapter", e.message.toString())
                }
            }

            binding.notesBtn.setOnClickListener {
                if (context is MainActivity) {
                    context.viewNoteForRecordId(record.id)
                }
            }

            binding.recordRow.setOnClickListener {
                if (context is MainActivity) {
                    context.updateRecord(record)
                }
            }

            binding.recordRow.setOnLongClickListener {
                if (context is MainActivity) {
                    context.deleteRecord(record)
                }
                true
            }
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }


}