package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.spudg.sentient.databinding.ActivityVisualiserBinding
import com.spudg.sentient.databinding.MonthYearPickerBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class VisualiserActivity : AppCompatActivity() {

    private var entriesPie: ArrayList<PieEntry> = ArrayList()
    private var entriesBar: ArrayList<BarEntry> = ArrayList()

    private lateinit var bindingVisualiser: ActivityVisualiserBinding
    private lateinit var bindingMonthYearPicker: MonthYearPickerBinding

    private var daysInMonth: ArrayList<Int> = ArrayList()
    private var averageScoresPerDay: ArrayList<Int> = ArrayList()

    var scoreSplit: ArrayList<Int> = arrayListOf()
    var scoreTitles: ArrayList<String> = arrayListOf()

    var monthFilter = Calendar.getInstance()[Calendar.MONTH] + 1
    var yearFilter = Calendar.getInstance()[Calendar.YEAR]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingVisualiser = ActivityVisualiserBinding.inflate(layoutInflater)
        val view = bindingVisualiser.root
        setContentView(view)

        setMonthHeader(monthFilter, yearFilter)
        setUpScoreNumberText()
        makeBarData(monthFilter, yearFilter)
        makePieData(monthFilter, yearFilter)
        setUpBarChart()
        setUpPieChart()
        setUpNoteList()

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
                setMonthHeader(monthFilter, yearFilter)
                setUpScoreNumberText()
                makeBarData(monthFilter, yearFilter)
                makePieData(monthFilter, yearFilter)
                setUpBarChart()
                setUpPieChart()
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

    }

    private fun resetBarData() {
        entriesBar = arrayListOf()
        daysInMonth = arrayListOf()
        averageScoresPerDay = arrayListOf()
    }

    private fun makeBarData(monthFilter: Int, yearFilter: Int) {

        resetBarData()

        val db = RecordHandler(this, null)

        daysInMonth = if (yearFilter % 4 == 0) {
            when (monthFilter) {
                1, 3, 5, 7, 8, 10, 12 -> Globals.DAYS31
                4, 6, 9, 11 -> Globals.DAYS30
                2 -> Globals.DAYS29
                else -> arrayListOf(0)
            }
        } else {
            when (monthFilter) {
                1, 3, 5, 7, 8, 10, 12 -> Globals.DAYS31
                4, 6, 9, 11 -> Globals.DAYS30
                2 -> Globals.DAYS28
                else -> arrayListOf(0)
            }
        }

        for (day in daysInMonth) {
            var averageForDay = db.getAveScoreForDayMonthYear(
                    day,
                    monthFilter,
                    yearFilter
            )
            averageScoresPerDay.add(averageForDay)
        }

        db.close()
    }

    private fun setUpBarChart() {

        var runningTotal = 0

        for (score in averageScoresPerDay) {
            runningTotal += score
        }

        if (runningTotal != 0) {
            for (i in 0 until daysInMonth.size) {
                entriesBar.add(BarEntry(daysInMonth[i].toFloat(), averageScoresPerDay[i].toFloat()))


                val dataSetBar = BarDataSet(entriesBar, "")
                val dataBar = BarData(dataSetBar)
                //dataSetBar.color = categoryColour

                dataBar.setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) {
                            super.getFormattedValue(value)
                        } else {
                            ""
                        }
                    }
                })

                val chartBar: BarChart = bindingVisualiser.chartAverageDaily
                if (entriesBar.size > 0) {
                    chartBar.data = dataBar
                }

                chartBar.animateY(800)
                chartBar.setNoDataText("No data for the month and category selected.")
                chartBar.setNoDataTextColor(0xff000000.toInt())
                chartBar.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
                chartBar.xAxis.setDrawGridLines(false)
                chartBar.axisRight.isEnabled = false
                chartBar.xAxis.position = XAxis.XAxisPosition.BOTTOM
                chartBar.legend.isEnabled = false

                chartBar.description.isEnabled = false

                val l: Legend = chartBar.legend
                l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                l.orientation = Legend.LegendOrientation.HORIZONTAL
                l.setDrawInside(false)

                chartBar.invalidate()
            }
        } else {
            bindingVisualiser.chartAverageDaily.clear()
            bindingVisualiser.chartAverageDaily.setNoDataText("No data for the month and category selected.")
            bindingVisualiser.chartAverageDaily.setNoDataTextColor(0xff000000.toInt())
            bindingVisualiser.chartAverageDaily.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
        }
    }

    private fun resetPieData() {
        scoreSplit = arrayListOf()
        scoreTitles = arrayListOf()
        entriesPie = arrayListOf()
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

        scoreSplit = arrayListOf(score0to9, score10to39, score40to69, score70to89, score90to100)
        scoreTitles = arrayListOf("0 to 9", "10 to 39", "40 to 69", "70 to 89", "90 to 100")

        db.close()

    }

    private fun setUpPieChart() {

        if (scoreSplit.size > 0) {
            for (i in 0 until scoreSplit.size) {
                entriesPie.add(PieEntry(scoreSplit[i].toFloat(), scoreTitles[i]))
            }

            val splitColours = ArrayList<Int>()
            splitColours.add(-65527)
            splitColours.add(-25088)
            splitColours.add(-16728577)
            splitColours.add(-16711896)
            splitColours.add(-6881025)

            val dataSetPie = PieDataSet(entriesPie, "")
            dataSetPie.colors = splitColours.toMutableList()
            val dataPie = PieData(dataSetPie)

            val chartPie: PieChart = bindingVisualiser.chartSplitAveScore
            if (entriesPie.size > 0) {
                chartPie.data = dataPie
            }

            chartPie.animateY(800)
            chartPie.setNoDataText("No records posted for the month selected.")
            chartPie.setNoDataTextColor(0xff000000.toInt())
            chartPie.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            chartPie.setEntryLabelTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            chartPie.dragDecelerationFrictionCoef = .95f
            chartPie.setDrawEntryLabels(false)

            chartPie.description.isEnabled = false

            val l: Legend = chartPie.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)

            dataSetPie.valueLinePart1OffsetPercentage = 80f
            dataSetPie.valueLinePart1Length = 0.4f
            dataSetPie.valueLinePart2Length = 0.8f
            dataSetPie.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSetPie.valueFormatter = WholeNumberRecordFormatter()

            dataPie.setValueTextSize(11f)
            dataPie.setValueTextColor(Color.BLACK)

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