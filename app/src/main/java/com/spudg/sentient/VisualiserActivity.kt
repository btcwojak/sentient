package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.spudg.sentient.databinding.ActivityVisualiserBinding
import com.spudg.sentient.databinding.DayMonthYearPickerBinding
import com.spudg.sentient.databinding.MonthYearPickerBinding
import java.util.*
import kotlin.collections.ArrayList

class VisualiserActivity : AppCompatActivity() {

    private var entriesPie: ArrayList<PieEntry> = ArrayList()
    private var entriesBar: ArrayList<BarEntry> = ArrayList()

    private lateinit var bindingVisualiser: ActivityVisualiserBinding
    private lateinit var bindingMonthYearPicker: MonthYearPickerBinding

    var scoreSplit: ArrayList<Int> = arrayListOf()
    var scoreTitles: ArrayList<String> = arrayListOf()

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

                makeBarData(monthFilter, yearFilter)
                makePieData(monthFilter, yearFilter)

                setUpBarChart()
                setUpPieChart()

                setMonthHeader(monthFilter, yearFilter)

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

        setMonthHeader(monthFilter, yearFilter)

        setUpScoreNumberText()

        makeBarData(monthFilter,yearFilter)
        setUpBarChart()

        makePieData(monthFilter,yearFilter)
        setUpPieChart()

        setUpNoteList()


    }

    private fun resetBarData() {

    }

    private fun makeBarData(monthFilter: Int, yearFilter: Int) {

    }

    private fun setUpBarChart() {

    }

    private fun resetPieData() {
        scoreSplit = arrayListOf()
        scoreTitles = arrayListOf()
    }

    private fun makePieData(monthFilter: Int, yearFilter: Int) {

        resetPieData()

        val db = RecordHandler(this, null)

        val records = db.getRecordsForMonthYear(monthFilter, yearFilter)

        var score0to9 = 0
        var score10to39 = 0
        var score40to69 = 0
        var score70to89 = 0
        var score90to100 = 0

        for (record in records) {
            when (record.score) {
                in 0..9 -> {
                    score0to9 += 1
                }
                in 10..39 -> {
                    score10to39 += 1
                }
                in 40..69 -> {
                    score40to69 += 1
                }
                in 70..89 -> {
                    score70to89 += 1
                }
                in 90..100 -> {
                    score90to100 += 1
                }

            }
        }

        scoreSplit = arrayListOf(score0to9,score10to39,score40to69,score70to89,score90to100)
        scoreTitles = arrayListOf("0 to 9","10 to 39", "40 to 69", "70 to 89", "90 to 100")

        db.close()

    }

    private fun setUpPieChart() {

        if (scoreSplit.size > 0) {
            for (i in 0 until scoreSplit.size) {
                entriesPie.add(PieEntry(scoreSplit[i].toFloat(), scoreTitles[i]))
            }

            val dataSetPie = PieDataSet(entriesPie, "")
            //dataSetExp.colors = categoryColoursExp.toMutableList()
            val dataPie = PieData(dataSetPie)

            val chartPie: PieChart = bindingVisualiser.chartSplitAveScore
            if (entriesPie.size > 0) {
                chartPie.data = dataPie
            }

            //chartExp.animateY(800)
            //chartExp.setNoDataText("No net expenditure categories for the month selected.")
            //chartExp.setNoDataTextColor(0xff000000.toInt())
            //chartExp.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            //chartExp.setEntryLabelTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            //chartExp.dragDecelerationFrictionCoef = .95f
            //chartExp.setDrawEntryLabels(false)

            //chartExp.description.isEnabled = false

            //val l: Legend = chartExp.legend
            //l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            //l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            //l.orientation = Legend.LegendOrientation.HORIZONTAL
            //l.setDrawInside(false)

            //dataSetExp.valueLinePart1OffsetPercentage = 80f
            //dataSetExp.valueLinePart1Length = 0.4f
            //dataSetExp.valueLinePart2Length = 0.8f
            //dataSetExp.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

            //dataExp.setValueFormatter(PercentFormatter())
            //dataExp.setValueTextSize(11f)
            //dataExp.setValueTextColor(Color.BLACK)

            chartPie.invalidate()

        } else {
            bindingVisualiser.chartSplitAveScore.clear()
            bindingVisualiser.chartSplitAveScore.setNoDataText("No net expenditure categories for the month selected.")
            bindingVisualiser.chartSplitAveScore.setNoDataTextColor(0xff000000.toInt())
            bindingVisualiser.chartSplitAveScore.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
        }
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