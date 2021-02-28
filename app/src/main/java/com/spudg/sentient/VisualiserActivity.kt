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
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.spudg.sentient.databinding.ActivityVisualiserBinding
import com.spudg.sentient.databinding.MonthYearPickerBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class VisualiserActivity : AppCompatActivity() {

    private var entriesPie: ArrayList<PieEntry> = ArrayList()
    private var entriesBarDaily: ArrayList<BarEntry> = ArrayList()
    private var entriesBarMonthly: ArrayList<BarEntry> = ArrayList()

    private lateinit var bindingVisualiser: ActivityVisualiserBinding
    private lateinit var bindingMonthYearPicker: MonthYearPickerBinding

    private var daysInMonth: ArrayList<Int> = ArrayList()
    private var averageScoresPerDay: ArrayList<Int> = ArrayList()
    private var averageScoresPerMonth: ArrayList<Int> = ArrayList()

    private var scoreSplitPie: ArrayList<Int> = arrayListOf()
    private var scoreTitlesPie: ArrayList<String> = arrayListOf()
    private var scoreColoursPie: ArrayList<Int> = arrayListOf()

    private var monthFilter = Calendar.getInstance()[Calendar.MONTH] + 1
    private var yearFilter = Calendar.getInstance()[Calendar.YEAR]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingVisualiser = ActivityVisualiserBinding.inflate(layoutInflater)
        val view = bindingVisualiser.root
        setContentView(view)

        setMonthHeader(monthFilter, yearFilter)
        setMonthlyBarHeader(yearFilter)
        setUpScoreNumberText()
        makeBarDataDaily(monthFilter, yearFilter)
        makePieData(monthFilter, yearFilter)
        makeBarDataMonthly(yearFilter)
        setUpBarChartDaily()
        setUpPieChart()
        setUpBarChartMonthly()
        setUpNoteList()

        bindingVisualiser.selectNewMonthHeader.setOnClickListener {

            val filterDialog = Dialog(this, R.style.Theme_Dialog)
            filterDialog.setCancelable(false)
            bindingMonthYearPicker = MonthYearPickerBinding.inflate(layoutInflater)
            val viewMonthYearPicker = bindingMonthYearPicker.root
            filterDialog.setContentView(viewMonthYearPicker)
            filterDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            bindingMonthYearPicker.mypYear.minValue = 1000
            bindingMonthYearPicker.mypYear.maxValue = 2999
            bindingMonthYearPicker.mypMonth.minValue = 1
            bindingMonthYearPicker.mypMonth.maxValue = 12
            bindingMonthYearPicker.mypMonth.displayedValues = Globals.monthsShortArray

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
                setMonthlyBarHeader(yearFilter)
                setUpScoreNumberText()
                makeBarDataDaily(monthFilter, yearFilter)
                makePieData(monthFilter, yearFilter)
                makeBarDataMonthly(yearFilter)
                setUpBarChartDaily()
                setUpPieChart()
                setUpBarChartMonthly()
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

    private fun resetBarDataDaily() {
        entriesBarDaily = arrayListOf()
        daysInMonth = arrayListOf()
        averageScoresPerDay = arrayListOf()
    }

    private fun makeBarDataDaily(monthFilter: Int, yearFilter: Int) {

        resetBarDataDaily()

        //val db = RecordHandler(this, null)

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
            //val averageForDay = db.getAveScoreForDayMonthYear(
            //    day,
            //    monthFilter,
            //    yearFilter
            //)
            //averageScoresPerDay.add(averageForDay)
        }

        //db.close()
    }

    private fun setUpBarChartDaily() {

        var runningTotal = 0
        val colours = ArrayList<Int>()

        for (score in averageScoresPerDay) {
            runningTotal += score
            when (score) {
                in 0..9 -> {
                    colours.add(-65527)
                }
                in 10..39 -> {
                    colours.add(-25088)
                }
                in 40..69 -> {
                    colours.add(-16728577)
                }
                in 70..89 -> {
                    colours.add(-16711896)
                }
                in 90..100 -> {
                    colours.add(-6881025)
                }
            }
        }

        if (runningTotal != 0) {
            for (i in 0 until daysInMonth.size) {
                entriesBarDaily.add(
                    BarEntry(
                        daysInMonth[i].toFloat(),
                        averageScoresPerDay[i].toFloat()
                    )
                )


                val dataSetBarDaily = BarDataSet(entriesBarDaily, "")
                val dataBarDaily = BarData(dataSetBarDaily)
                dataSetBarDaily.colors = colours

                dataBarDaily.setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) {
                            val mFormat = DecimalFormat("###,###,##0")
                            mFormat.format(super.getFormattedValue(value).toFloat())
                        } else {
                            ""
                        }
                    }
                })

                val chartBarDaily: BarChart = bindingVisualiser.chartAverageDaily
                if (entriesBarDaily.size > 0) {
                    chartBarDaily.data = dataBarDaily
                }

                chartBarDaily.animateY(800)
                chartBarDaily.setNoDataText("No records for the month and year selected.")
                chartBarDaily.setNoDataTextColor(0xff000000.toInt())
                chartBarDaily.setNoDataTextTypeface(
                    ResourcesCompat.getFont(
                        this,
                        R.font.open_sans_light
                    )
                )
                chartBarDaily.xAxis.setDrawGridLines(false)
                chartBarDaily.axisRight.isEnabled = false
                chartBarDaily.xAxis.position = XAxis.XAxisPosition.BOTTOM
                chartBarDaily.legend.isEnabled = false
                chartBarDaily.axisRight.setDrawLabels(true)

                chartBarDaily.xAxis.labelCount = 15

                chartBarDaily.description.isEnabled = false

                chartBarDaily.invalidate()
            }
        } else {
            bindingVisualiser.chartAverageDaily.clear()
            bindingVisualiser.chartAverageDaily.setNoDataText("No data for the month and year selected.")
            bindingVisualiser.chartAverageDaily.setNoDataTextColor(0xff000000.toInt())
            bindingVisualiser.chartAverageDaily.setNoDataTextTypeface(
                ResourcesCompat.getFont(
                    this,
                    R.font.open_sans_light
                )
            )
        }
    }

    private fun resetPieData() {
        scoreSplitPie = arrayListOf()
        scoreTitlesPie = arrayListOf()
        scoreColoursPie = arrayListOf()
        entriesPie = arrayListOf()
    }

    private fun makePieData(monthFilter: Int, yearFilter: Int) {

        resetPieData()

        //val db = RecordHandler(this, null)

        //val records = db.getRecordsForMonthYear(monthFilter, yearFilter)

        var score0to9 = 0
        var score10to39 = 0
        var score40to69 = 0
        var score70to89 = 0
        var score90to100 = 0

        //for (record in records) {
        //    when (record.score) {
        //        in 0..9 -> {
        //            score0to9 += 1
        //        }
        //        in 10..39 -> {
        //            score10to39 += 1
        //        }
        //        in 40..69 -> {
        //            score40to69 += 1
        //        }
        //        in 70..89 -> {
        //            score70to89 += 1
        //        }
        //        in 90..100 -> {
        //            score90to100 += 1
        //        }
        //
        //    }
        //}

        if (score0to9 != 0) {
            scoreSplitPie.add(score0to9)
            scoreTitlesPie.add("0 to 9")
            scoreColoursPie.add(-65527)
        }
        if (score10to39 != 0) {
            scoreSplitPie.add(score10to39)
            scoreTitlesPie.add("10 to 39")
            scoreColoursPie.add(-25088)
        }
        if (score40to69 != 0) {
            scoreSplitPie.add(score40to69)
            scoreTitlesPie.add("40 to 69")
            scoreColoursPie.add(-16728577)
        }
        if (score70to89 != 0) {
            scoreSplitPie.add(score70to89)
            scoreTitlesPie.add("70 to 89")
            scoreColoursPie.add(-16711896)
        }
        if (score90to100 != 0) {
            scoreSplitPie.add(score90to100)
            scoreTitlesPie.add("90 to 100")
            scoreColoursPie.add(-6881025)
        }

        //db.close()

    }

    private fun setUpPieChart() {

        if (scoreSplitPie.size > 0) {
            for (i in 0 until scoreSplitPie.size) {
                entriesPie.add(PieEntry(scoreSplitPie[i].toFloat(), scoreTitlesPie[i]))
            }

            val dataSetPie = PieDataSet(entriesPie, "")
            dataSetPie.colors = scoreColoursPie
            val dataPie = PieData(dataSetPie)

            val chartPie: PieChart = bindingVisualiser.chartSplitAveScore
            if (entriesPie.size > 0) {
                chartPie.data = dataPie
            }

            chartPie.animateY(800)
            chartPie.setNoDataText("No records for the month and year selected.")
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
            val l1 = LegendEntry("0 to 9", Legend.LegendForm.CIRCLE, 10f, 2f, null, -65527)
            val l2 = LegendEntry("10 to 39", Legend.LegendForm.CIRCLE, 10f, 2f, null, -25088)
            val l3 = LegendEntry("40 to 69", Legend.LegendForm.CIRCLE, 10f, 2f, null, -16728577)
            val l4 = LegendEntry("70 to 89", Legend.LegendForm.CIRCLE, 10f, 2f, null, -16711896)
            val l5 = LegendEntry("90 to 100", Legend.LegendForm.CIRCLE, 10f, 2f, null, -6881025)
            l.setCustom(arrayOf(l1, l2, l3, l4, l5))

            dataSetPie.valueLinePart1OffsetPercentage = 80f
            dataSetPie.valueLinePart1Length = 0.4f
            dataSetPie.valueLinePart2Length = 0.8f
            dataSetPie.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

            dataPie.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) {
                        val mFormat = DecimalFormat("###,###,##0 records")
                        mFormat.format(super.getFormattedValue(value).toFloat())
                    } else {
                        ""
                    }
                }
            })

            dataPie.setValueTextSize(11f)
            dataPie.setValueTextColor(Color.BLACK)

            chartPie.invalidate()

        } else {
            bindingVisualiser.chartSplitAveScore.clear()
            bindingVisualiser.chartSplitAveScore.setNoDataText("No records for the month and year selected.")
            bindingVisualiser.chartSplitAveScore.setNoDataTextColor(0xff000000.toInt())
            bindingVisualiser.chartSplitAveScore.setNoDataTextTypeface(
                ResourcesCompat.getFont(
                    this,
                    R.font.open_sans_light
                )
            )
        }
    }

    private fun resetBarDataMonthly() {
        entriesBarMonthly = arrayListOf()
        averageScoresPerMonth = arrayListOf()
    }

    private fun makeBarDataMonthly(yearFilter: Int) {

        resetBarDataMonthly()

        //val db = RecordHandler(this, null)

        //repeat(12) {
        //    val averageForMonth = db.getAveScoreForMonthYear(
        //        it + 1,
        //        yearFilter,
        //    )
        //    averageScoresPerMonth.add(averageForMonth)
        //}

        //db.close()
    }

    private fun setUpBarChartMonthly() {

        var runningTotal = 0
        val colours = ArrayList<Int>()

        for (score in averageScoresPerMonth) {
            runningTotal += score
            when (score) {
                in 0..9 -> {
                    colours.add(-65527)
                }
                in 10..39 -> {
                    colours.add(-25088)
                }
                in 40..69 -> {
                    colours.add(-16728577)
                }
                in 70..89 -> {
                    colours.add(-16711896)
                }
                in 90..100 -> {
                    colours.add(-6881025)
                }
            }
        }

        if (runningTotal != 0) {

            for (i in 1..12) {
                entriesBarMonthly.add(
                    BarEntry(
                        (i).toFloat(),
                        averageScoresPerMonth[i - 1].toFloat()
                    )
                )
            }

            val dataSetBarMonthly = BarDataSet(entriesBarMonthly, "")
            val dataBarMonthly = BarData(dataSetBarMonthly)
            dataSetBarMonthly.colors = colours

            dataBarMonthly.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) {
                        val mFormat = DecimalFormat("###,###,##0")
                        mFormat.format(super.getFormattedValue(value).toFloat())
                    } else {
                        ""
                    }
                }
            })

            val chartBarMonthly: BarChart = bindingVisualiser.chartAverageMonthly
            if (entriesBarMonthly.size > 0) {
                chartBarMonthly.data = dataBarMonthly
            }

            chartBarMonthly.animateY(800)
            chartBarMonthly.setNoDataText("No records for the month and year selected.")
            chartBarMonthly.setNoDataTextColor(0xff000000.toInt())
            chartBarMonthly.setNoDataTextTypeface(
                ResourcesCompat.getFont(
                    this,
                    R.font.open_sans_light
                )
            )
            chartBarMonthly.xAxis.setDrawGridLines(false)
            chartBarMonthly.axisRight.isEnabled = false
            chartBarMonthly.xAxis.position = XAxis.XAxisPosition.BOTTOM
            chartBarMonthly.legend.isEnabled = false

            chartBarMonthly.xAxis.valueFormatter =
                IndexAxisValueFormatter(Globals.monthsShortArrayEmptyFirstEntry)
            chartBarMonthly.xAxis.labelCount = 12

            chartBarMonthly.description.isEnabled = false

            chartBarMonthly.invalidate()

        } else {
            bindingVisualiser.chartAverageMonthly.clear()
            bindingVisualiser.chartAverageMonthly.setNoDataText("No records for the month and year selected.")
            bindingVisualiser.chartAverageMonthly.setNoDataTextColor(0xff000000.toInt())
            bindingVisualiser.chartAverageMonthly.setNoDataTextTypeface(
                ResourcesCompat.getFont(
                    this,
                    R.font.open_sans_light
                )
            )
        }
    }

    private fun setUpNoteList() {
        if (getRecordListMonthYearWithNoteOnly(monthFilter, yearFilter).size > 0) {
            bindingVisualiser.rvNotes.visibility = View.VISIBLE
            bindingVisualiser.tvNoNotes.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingVisualiser.rvNotes.layoutManager = manager
            val noteAdapter =
                NoteListAdapter(getRecordListMonthYearWithNoteOnly(monthFilter, yearFilter))
            bindingVisualiser.rvNotes.adapter = noteAdapter
        } else {
            bindingVisualiser.rvNotes.visibility = View.GONE
            bindingVisualiser.tvNoNotes.visibility = View.VISIBLE
        }
    }

    private fun getRecordListMonthYearWithNoteOnly(month: Int, year: Int): ArrayList<RecordModel> {
        //val dbHandler = RecordHandler(this, null)
        //val result = dbHandler.getRecordsForMonthYearWithNoteOnly(month, year)
        //dbHandler.close()
        return ArrayList<RecordModel>()
    }

    private fun setUpScoreNumberText() {
        when (val numberScores = getNumberScoresMonthYear(monthFilter, yearFilter)) {
            0 -> {
                bindingVisualiser.numberScoresMonth.text = getString(R.string.no_scores_posted)
            }
            1 -> {
                bindingVisualiser.numberScoresMonth.text = getString(R.string.one_score_posted)
            }
            else -> {
                bindingVisualiser.numberScoresMonth.text =
                    getString(R.string.number_scores_posted, numberScores.toString())
            }
        }
    }

    private fun setMonthHeader(month: Int, year: Int) {
        bindingVisualiser.monthSelectedHeader.text =
            getString(R.string.month_year, Globals.monthsShortArray[month - 1], year.toString())
    }

    private fun setMonthlyBarHeader(year: Int) {
        bindingVisualiser.averageMonthlyScoresHeading.text =
            getString(R.string.average_monthly_scores_visualiser_heading, year.toString())
    }

    private fun getNumberScoresMonthYear(month: Int, year: Int): Int {
        //val db = RecordHandler(this, null)
        //return db.getRecordsForMonthYear(month, year).size
        return 1
    }

}