package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.spudg.sentient.databinding.ActivityVisualiserBinding
import com.spudg.sentient.databinding.DayMonthYearPickerBinding
import com.spudg.sentient.databinding.MonthYearPickerBinding
import java.util.*
import kotlin.collections.ArrayList

class VisualiserActivity : AppCompatActivity() {

    private lateinit var bindingVisualiser: ActivityVisualiserBinding
    private lateinit var bindingMonthYearPicker: MonthYearPickerBinding

    var monthFilter = Calendar.getInstance()[Calendar.MONTH] + 1
    var yearFilter = Calendar.getInstance()[Calendar.YEAR]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingVisualiser = ActivityVisualiserBinding.inflate(layoutInflater)
        val view = bindingVisualiser.root
        setContentView(view)

        bindingVisualiser.selectNewMonthHeader.setOnClickListener {

            val filterDialog = Dialog(this, R.style.Theme_Dialog)
            filterDialog.setCancelable(false)
            bindingMonthYearPicker = MonthYearPickerBinding.inflate(layoutInflater)
            val view = bindingMonthYearPicker.root
            filterDialog.setContentView(view)
            filterDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            bindingMonthYearPicker.mypYear.minValue = 1000
            bindingMonthYearPicker.mypYear.maxValue = 2999
            bindingMonthYearPicker.mypMonth.minValue = 1
            bindingMonthYearPicker.mypMonth.maxValue = 12
            bindingMonthYearPicker.mypMonth.displayedValues = monthsShortArray

            bindingMonthYearPicker.mypYear.wrapSelectorWheel = true
            bindingMonthYearPicker.mypMonth.wrapSelectorWheel = true
            bindingMonthYearPicker.mypYear.value = yearFilter
            bindingMonthYearPicker.mypMonth.value = monthFilter

            bindingMonthYearPicker.mypMonth.setOnValueChangedListener { _, _, newVal ->
                monthFilter = newVal
            }

            bindingMonthYearPicker.mypYear.setOnValueChangedListener { _, _, newVal ->
                yearFilter = newVal
            }

            bindingMonthYearPicker.submitMy.setOnClickListener {
                //makePieData(monthFilter, yearFilter)
                //makeBarData(monthFilter, yearFilter, 1)
                setMonthHeader(monthFilter, yearFilter)
                //setupPieChartIncome()
                //setupPieChartExpenditure()
                //setupBarChart()
                setUpScoreNumberText()
                setUpNoteList()
                filterDialog.dismiss()
            }

            bindingMonthYearPicker.cancelMy.setOnClickListener {
                filterDialog.dismiss()
            }

            filterDialog.show()

        }

        bindingVisualiser.backToRecordsFromVisualiser.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setUpScoreNumberText()
        setUpNoteList()
        setMonthHeader(monthFilter, yearFilter)

    }

    private fun setUpNoteList() {
        if (getRecordListMonthYearWithNoteOnly(monthFilter, yearFilter).size > 0) {
            bindingVisualiser.rvNotes.visibility = View.VISIBLE
            bindingVisualiser.tvNoNotes.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingVisualiser.rvNotes.layoutManager = manager
            val noteAdapter = NoteListAdapter(this, getRecordListMonthYearWithNoteOnly(monthFilter, yearFilter))
            bindingVisualiser.rvNotes.adapter = noteAdapter
        } else {
            bindingVisualiser.rvNotes.visibility = View.GONE
            bindingVisualiser.tvNoNotes.visibility = View.VISIBLE
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

    private fun getRecordListMonthYearWithNoteOnly(month: Int, year: Int): ArrayList<RecordModel> {
        val dbHandler = RecordHandler(this, null)
        val result = dbHandler.getRecordsForMonthYearWithNoteOnly(month, year)
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

    private fun setMonthHeader(month: Int, year: Int) {
        bindingVisualiser.monthSelectedHeader.text = "${monthsShortArray[month - 1]} $year"
    }

    private fun getNumberScoresMonthYear(month: Int, year: Int): Int {
        val db = RecordHandler(this, null)
        return db.getRecordsForMonthYear(month, year).size
    }

    private var monthsShortArray: Array<String> = arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
    )

}