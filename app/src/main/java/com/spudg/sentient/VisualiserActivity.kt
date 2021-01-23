package com.spudg.sentient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.spudg.sentient.databinding.ActivityVisualiserBinding
import java.util.*

class VisualiserActivity : AppCompatActivity() {

    private lateinit var bindingVisualiser: ActivityVisualiserBinding

    var monthFilter = Calendar.getInstance()[Calendar.MONTH] + 1
    var yearFilter = Calendar.getInstance()[Calendar.YEAR]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingVisualiser = ActivityVisualiserBinding.inflate(layoutInflater)
        val view = bindingVisualiser.root
        setContentView(view)

        bindingVisualiser.backToRecordsFromVisualiser.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setUpScoreNumberText()
        setUpNoteList()

    }

    private fun setUpNoteList() {
        if (getRecordListMonthYear(monthFilter, yearFilter).size > 0) {
            bindingVisualiser.rvNotes.visibility = View.VISIBLE
            //bindingMain.tvNoRecords.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingVisualiser.rvNotes.layoutManager = manager
            val noteAdapter = NoteListAdapter(this, getRecordListMonthYear(monthFilter, yearFilter))
            bindingVisualiser.rvNotes.adapter = noteAdapter
        } else {
            bindingVisualiser.rvNotes.visibility = View.GONE
            //bindingMain.tvNoRecords.visibility = View.VISIBLE
        }
    }

    private fun getRecordList(): ArrayList<RecordModel> {
        val dbHandler = RecordHandler(this, null)
        val result = dbHandler.filterRecords(-1)
        dbHandler.close()
        return result
    }

    private fun getRecordListMonthYear(month: Int, year: Int): ArrayList<RecordModel> {
        val dbHandler = RecordHandler(this, null)
        val result = dbHandler.getRecordsForMonthYear(month, year)
        dbHandler.close()
        return result
    }

    private fun setUpScoreNumberText() {
        var numberScores = getNumberScoresMonthYear(monthFilter, yearFilter)
        when (numberScores) {
            0 -> {
                bindingVisualiser.numberScoresMonth.text = "No scores posted yet this month"
            }
            1 -> {
                bindingVisualiser.numberScoresMonth.text = "1 score posted this month"
            }
            else -> {
                bindingVisualiser.numberScoresMonth.text = "$numberScores scores posted this month"
            }
        }
    }

    private fun getNumberScoresMonthYear(month: Int, year: Int): Int {
        val db = RecordHandler(this, null)
        return db.getRecordsForMonthYear(month, year).size
    }

}