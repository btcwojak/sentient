package com.spudg.sentient

import android.app.AlarmManager
import android.app.Dialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.spudg.sentient.databinding.*
import java.text.SimpleDateFormat
import java.time.Instant.now
import java.time.LocalDateTime.now
import java.time.Month
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingAddRecord: DialogAddRecordBinding
    private lateinit var bindingUpdateRecord: DialogUpdateRecordBinding
    private lateinit var bindingDeleteRecord: DialogDeleteRecordBinding
    private lateinit var bindingDMYP: DayMonthYearPickerBinding
    private lateinit var bindingHMP: HourMinutePickerBinding
    private lateinit var bindingViewNote: DialogViewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)

        bindingMain.visualiserBtn.setOnClickListener {
            val intent = Intent(this, VisualiserActivity::class.java)
            startActivity(intent)
        }

        bindingMain.aboutBtn.setOnClickListener {

        }

        bindingMain.addRecord.setOnClickListener {
            addRecord()
        }

        setUpRecordList()
        setUpAverageMonthScore()

    }

    private fun setUpAverageMonthScore() {
        val dbHandler = RecordHandler(this, null)
        val allRecords = dbHandler.filterRecords()

        var runningTotal = 0
        var numberOfRatings = 0

        var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        var currentYear = Calendar.getInstance().get(Calendar.YEAR)
        var currentDate = currentMonth.toString() + currentYear.toString()

        val cal = Calendar.getInstance()

        for (record in allRecords) {

            cal.timeInMillis = record.time.toLong()
            val recordMonth = cal.get(Calendar.MONTH) + 1
            val recordYear = cal.get(Calendar.YEAR)
            val recordDate = recordMonth.toString() + recordYear.toString()

            if (recordDate == currentDate) {
                numberOfRatings += 1
                runningTotal += record.score
            }

        }

        if (numberOfRatings > 0) {

            var averageScore = (runningTotal/numberOfRatings)
            bindingMain.averageScoreMonth.text = averageScore.toString()

            when (averageScore) {
                in 0..9 -> {
                    bindingMain.averageScoreMonth.setTextColor(-65527)
                }
                in 10..39 -> {
                    bindingMain.averageScoreMonth.setTextColor(-25088)
                }
                in 40..69 -> {
                    bindingMain.averageScoreMonth.setTextColor(-16728577)
                }
                in 70..89 -> {
                    bindingMain.averageScoreMonth.setTextColor(-16711896)
                }
                in 90..100 -> {
                    bindingMain.averageScoreMonth.setTextColor(-6881025)
                }
            }
        }
    }

    private fun setUpRecordList() {
        if (getRecordList().size > 0) {
            bindingMain.rvRecords.visibility = View.VISIBLE
            bindingMain.tvNoRecords.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingMain.rvRecords.layoutManager = manager
            val policyAdapter = RecordAdapter(this, getRecordList())
            bindingMain.rvRecords.adapter = policyAdapter
        } else {
            bindingMain.rvRecords.visibility = View.GONE
            bindingMain.tvNoRecords.visibility = View.VISIBLE
        }
    }

    private fun getRecordList(): ArrayList<RecordModel> {
        val dbHandler = RecordHandler(this, null)
        val result = dbHandler.filterRecords(-1)
        dbHandler.close()
        return result
    }

    private fun addRecord() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        bindingAddRecord = DialogAddRecordBinding.inflate(layoutInflater)
        val view = bindingAddRecord.root
        addDialog.setContentView(view)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddRecord.dateRecordPost.text =
                "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"

        bindingAddRecord.dateRecordPost.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYP = DayMonthYearPickerBinding.inflate(layoutInflater)
            val view = bindingDMYP.root
            changeDateDialog.setContentView(view)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                bindingDMYP.dmypDay.maxValue = 30
                bindingDMYP.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                bindingDMYP.dmypDay.maxValue = 29
                bindingDMYP.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                bindingDMYP.dmypDay.maxValue = 28
                bindingDMYP.dmypDay.minValue = 1
            } else {
                bindingDMYP.dmypDay.maxValue = 31
                bindingDMYP.dmypDay.minValue = 1
            }

            bindingDMYP.dmypMonth.maxValue = 12
            bindingDMYP.dmypMonth.minValue = 1
            bindingDMYP.dmypYear.maxValue = 2999
            bindingDMYP.dmypYear.minValue = 1000

            bindingDMYP.dmypDay.value = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            bindingDMYP.dmypMonth.value = Calendar.getInstance()[Calendar.MONTH] + 1
            bindingDMYP.dmypYear.value = Calendar.getInstance()[Calendar.YEAR]
            dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
            yearPicked = Calendar.getInstance()[Calendar.YEAR]

            bindingDMYP.dmypMonth.displayedValues = Globals.monthsShortArray

            bindingDMYP.dmypDay.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            bindingDMYP.dmypMonth.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    bindingDMYP.dmypDay.maxValue = 30
                    bindingDMYP.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYP.dmypYear.value % 4 == 0)) {
                    bindingDMYP.dmypDay.maxValue = 29
                    bindingDMYP.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYP.dmypYear.value % 4 != 0)) {
                    bindingDMYP.dmypDay.maxValue = 28
                    bindingDMYP.dmypDay.minValue = 1
                } else {
                    bindingDMYP.dmypDay.maxValue = 31
                    bindingDMYP.dmypDay.minValue = 1
                }
                monthPicked = newVal
            }

            bindingDMYP.dmypYear.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && bindingDMYP.dmypMonth.value == 2) {
                    bindingDMYP.dmypDay.maxValue = 29
                    bindingDMYP.dmypDay.minValue = 1
                } else if (newVal % 4 != 0 && bindingDMYP.dmypMonth.value == 2) {
                    bindingDMYP.dmypDay.maxValue = 28
                    bindingDMYP.dmypDay.minValue = 1
                }
                yearPicked = newVal
            }

            bindingDMYP.submitDmy.setOnClickListener {
                bindingAddRecord.dateRecordPost.text =
                        "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            bindingDMYP.dmypDay.wrapSelectorWheel = true
            bindingDMYP.dmypMonth.wrapSelectorWheel = true
            bindingDMYP.dmypYear.wrapSelectorWheel = true

            bindingDMYP.cancelDmy.setOnClickListener {
                dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
                monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
                yearPicked = Calendar.getInstance()[Calendar.YEAR]
                bindingAddRecord.dateRecordPost.text =
                        "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.show()

        }

        var hourPicked = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        var minutePicked = Calendar.getInstance()[Calendar.MINUTE]

        bindingAddRecord.timeRecordPost.text =
                "$hourPicked:$minutePicked"

        bindingAddRecord.timeRecordPost.setOnClickListener {
            val changeTimeDialog = Dialog(this, R.style.Theme_Dialog)
            changeTimeDialog.setCancelable(false)
            bindingHMP = HourMinutePickerBinding.inflate(layoutInflater)
            val view = bindingHMP.root
            changeTimeDialog.setContentView(view)
            changeTimeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingHMP.dmypHour.maxValue = 23
            bindingHMP.dmypHour.minValue = 1
            bindingHMP.dmypMinute.maxValue = 59
            bindingHMP.dmypMinute.minValue = 1

            bindingHMP.dmypHour.value = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            bindingHMP.dmypMinute.value = Calendar.getInstance()[Calendar.MINUTE]
            hourPicked = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            minutePicked = Calendar.getInstance()[Calendar.MINUTE]

            bindingHMP.dmypHour.setOnValueChangedListener { _, _, newVal ->
                hourPicked = newVal
            }

            bindingHMP.dmypMinute.setOnValueChangedListener { _, _, newVal ->
                minutePicked = newVal
            }

            bindingHMP.submitHm.setOnClickListener {
                bindingAddRecord.timeRecordPost.text =
                        "$hourPicked:$minutePicked"
                changeTimeDialog.dismiss()
            }

            bindingHMP.dmypHour.wrapSelectorWheel = true
            bindingHMP.dmypMinute.wrapSelectorWheel = true

            bindingHMP.cancelHm.setOnClickListener {
                hourPicked = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
                minutePicked = Calendar.getInstance()[Calendar.MINUTE]
                bindingAddRecord.timeRecordPost.text =
                        "$hourPicked:$minutePicked"
                changeTimeDialog.dismiss()
            }

            changeTimeDialog.show()

        }

        bindingAddRecord.scoreSliderPost.value = 50F
        bindingAddRecord.currentScorePost.text = bindingAddRecord.scoreSliderPost.value.roundToInt().toString()
        bindingAddRecord.currentScorePost.setTextColor(-16728577)

        bindingAddRecord.scoreSliderPost.addOnChangeListener { slider, value, fromUser ->
            slider.value = value.roundToInt().toFloat()
            bindingAddRecord.currentScorePost.text = value.roundToInt().toString()
            when (value) {
                in 0F..9F -> {
                    bindingAddRecord.currentScorePost.setTextColor(-65527)
                    slider.thumbTintList
                }
                in 10F..39F -> {
                    bindingAddRecord.currentScorePost.setTextColor(-25088)
                }
                in 40F..69F -> {
                    bindingAddRecord.currentScorePost.setTextColor(-16728577)
                }
                in 70F..89F -> {
                    bindingAddRecord.currentScorePost.setTextColor(-16711896)
                }
                in 90F..100F -> {
                    bindingAddRecord.currentScorePost.setTextColor(-6881025)
                }
            }
        }

        bindingAddRecord.tvPostRecord.setOnClickListener {

            val dbHandlerRecord = RecordHandler(this, null)

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked,monthPicked-1,dayPicked,hourPicked,minutePicked)

            val score = bindingAddRecord.scoreSliderPost.value.toInt()
            val time = calendar.timeInMillis.toString()
            val note = bindingAddRecord.etNotePostRecord.text.toString()

            if (score.toString().isNotEmpty() && time.isNotEmpty()) {
                dbHandlerRecord.addRecord(
                        RecordModel(
                                0,
                                score,
                                time,
                                note,
                        )
                )

                Toast.makeText(this, "Record posted.", Toast.LENGTH_LONG).show()
                setUpRecordList()
                setUpAverageMonthScore()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Mood can't be blank.", Toast.LENGTH_LONG)
                        .show()
            }

            dbHandlerRecord.close()

        }
        addDialog.show()

        bindingAddRecord.tvCancelPostRecord.setOnClickListener {
            addDialog.dismiss()
        }
    }

    fun updateRecord(record: RecordModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        bindingUpdateRecord = DialogUpdateRecordBinding.inflate(layoutInflater)
        val view = bindingUpdateRecord.root
        updateDialog.setContentView(view)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var calForOldRecordTime = Calendar.getInstance()
        calForOldRecordTime.timeInMillis = record.time.toLong()
        var oldDay = calForOldRecordTime.get(Calendar.DAY_OF_MONTH)
        var oldMonth = calForOldRecordTime.get(Calendar.MONTH) + 1
        var oldYear = calForOldRecordTime.get(Calendar.YEAR)
        var oldHour = calForOldRecordTime.get(Calendar.HOUR_OF_DAY)
        var oldMinute = calForOldRecordTime.get(Calendar.MINUTE)

        var dayPicked = oldDay
        var monthPicked = oldMonth
        var yearPicked = oldYear

        bindingUpdateRecord.dateRecordUpdate.text =
                "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"

        bindingUpdateRecord.dateRecordUpdate.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYP = DayMonthYearPickerBinding.inflate(layoutInflater)
            val view = bindingDMYP.root
            changeDateDialog.setContentView(view)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                bindingDMYP.dmypDay.maxValue = 30
                bindingDMYP.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                bindingDMYP.dmypDay.maxValue = 29
                bindingDMYP.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                bindingDMYP.dmypDay.maxValue = 28
                bindingDMYP.dmypDay.minValue = 1
            } else {
                bindingDMYP.dmypDay.maxValue = 31
                bindingDMYP.dmypDay.minValue = 1
            }

            bindingDMYP.dmypMonth.maxValue = 12
            bindingDMYP.dmypMonth.minValue = 1
            bindingDMYP.dmypYear.maxValue = 2999
            bindingDMYP.dmypYear.minValue = 1000

            bindingDMYP.dmypDay.value = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            bindingDMYP.dmypMonth.value = Calendar.getInstance()[Calendar.MONTH] + 1
            bindingDMYP.dmypYear.value = Calendar.getInstance()[Calendar.YEAR]
            dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
            yearPicked = Calendar.getInstance()[Calendar.YEAR]

            bindingDMYP.dmypMonth.displayedValues = Globals.monthsShortArray

            bindingDMYP.dmypDay.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            bindingDMYP.dmypMonth.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    bindingDMYP.dmypDay.maxValue = 30
                    bindingDMYP.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYP.dmypYear.value % 4 == 0)) {
                    bindingDMYP.dmypDay.maxValue = 29
                    bindingDMYP.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYP.dmypYear.value % 4 != 0)) {
                    bindingDMYP.dmypDay.maxValue = 28
                    bindingDMYP.dmypDay.minValue = 1
                } else {
                    bindingDMYP.dmypDay.maxValue = 31
                    bindingDMYP.dmypDay.minValue = 1
                }
                monthPicked = newVal
            }

            bindingDMYP.dmypYear.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && bindingDMYP.dmypMonth.value == 2) {
                    bindingDMYP.dmypDay.maxValue = 29
                    bindingDMYP.dmypDay.minValue = 1
                } else if (newVal % 4 != 0 && bindingDMYP.dmypMonth.value == 2) {
                    bindingDMYP.dmypDay.maxValue = 28
                    bindingDMYP.dmypDay.minValue = 1
                }
                yearPicked = newVal
            }

            bindingDMYP.submitDmy.setOnClickListener {
                bindingUpdateRecord.dateRecordUpdate.text =
                        "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            bindingDMYP.dmypDay.wrapSelectorWheel = true
            bindingDMYP.dmypMonth.wrapSelectorWheel = true
            bindingDMYP.dmypYear.wrapSelectorWheel = true

            bindingDMYP.cancelDmy.setOnClickListener {
                dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
                monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
                yearPicked = Calendar.getInstance()[Calendar.YEAR]
                bindingUpdateRecord.dateRecordUpdate.text =
                        "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.show()

        }

        var hourPicked = oldHour
        var minutePicked = oldMinute

        bindingUpdateRecord.timeRecordUpdate.text =
                "$hourPicked:$minutePicked"

        bindingUpdateRecord.timeRecordUpdate.setOnClickListener {
            val changeTimeDialog = Dialog(this, R.style.Theme_Dialog)
            changeTimeDialog.setCancelable(false)
            bindingHMP = HourMinutePickerBinding.inflate(layoutInflater)
            val view = bindingHMP.root
            changeTimeDialog.setContentView(view)
            changeTimeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingHMP.dmypHour.maxValue = 23
            bindingHMP.dmypHour.minValue = 1
            bindingHMP.dmypMinute.maxValue = 59
            bindingHMP.dmypMinute.minValue = 1

            bindingHMP.dmypHour.value = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            bindingHMP.dmypMinute.value = Calendar.getInstance()[Calendar.MINUTE]
            hourPicked = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            minutePicked = Calendar.getInstance()[Calendar.MINUTE]

            bindingHMP.dmypHour.setOnValueChangedListener { _, _, newVal ->
                hourPicked = newVal
            }

            bindingHMP.dmypMinute.setOnValueChangedListener { _, _, newVal ->
                minutePicked = newVal
            }

            bindingHMP.submitHm.setOnClickListener {
                bindingUpdateRecord.timeRecordUpdate.text =
                        "$hourPicked:$minutePicked"
                changeTimeDialog.dismiss()
            }

            bindingHMP.dmypHour.wrapSelectorWheel = true
            bindingHMP.dmypMinute.wrapSelectorWheel = true

            bindingHMP.cancelHm.setOnClickListener {
                hourPicked = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
                minutePicked = Calendar.getInstance()[Calendar.MINUTE]
                bindingUpdateRecord.timeRecordUpdate.text =
                        "$hourPicked:$minutePicked"
                changeTimeDialog.dismiss()
            }

            changeTimeDialog.show()

        }


        val notesET = bindingUpdateRecord.etNoteUpdateRecord
        notesET.setText(record.note)

        bindingUpdateRecord.scoreSliderUpdate.value = record.score.toFloat()
        bindingUpdateRecord.currentScoreUpdate.text = record.score.toString()
        when (record.score.toFloat()) {
            in 0F..9F -> {
                bindingUpdateRecord.currentScoreUpdate.setTextColor(-65527)
            }
            in 10F..39F -> {
                bindingUpdateRecord.currentScoreUpdate.setTextColor(-25088)
            }
            in 40F..69F -> {
                bindingUpdateRecord.currentScoreUpdate.setTextColor(-16728577)
            }
            in 70F..89F -> {
                bindingUpdateRecord.currentScoreUpdate.setTextColor(-16711896)
            }
            in 90F..100F -> {
                bindingUpdateRecord.currentScoreUpdate.setTextColor(-6881025)
            }
        }

        bindingUpdateRecord.scoreSliderUpdate.addOnChangeListener { slider, value, fromUser ->
            slider.value = value.roundToInt().toFloat()
            bindingUpdateRecord.currentScoreUpdate.text = value.roundToInt().toString()
            when (value) {
                in 0F..9F -> {
                    bindingUpdateRecord.currentScoreUpdate.setTextColor(-65527)
                    slider.thumbTintList
                }
                in 10F..39F -> {
                    bindingUpdateRecord.currentScoreUpdate.setTextColor(-25088)
                }
                in 40F..69F -> {
                    bindingUpdateRecord.currentScoreUpdate.setTextColor(-16728577)
                }
                in 70F..89F -> {
                    bindingUpdateRecord.currentScoreUpdate.setTextColor(-16711896)
                }
                in 90F..100F -> {
                    bindingUpdateRecord.currentScoreUpdate.setTextColor(-6881025)
                }
            }
        }

        bindingUpdateRecord.tvUpdateRecord.setOnClickListener {

            val dbHandlerRecord = RecordHandler(this, null)

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, dayPicked, hourPicked, minutePicked)

            val score = bindingUpdateRecord.scoreSliderUpdate.value.toInt()
            val time = calendar.timeInMillis.toString()
            val note = bindingUpdateRecord.etNoteUpdateRecord.text.toString()

            if (score.toString().isNotEmpty() && time.isNotEmpty()) {
                dbHandlerRecord.updateRecord(
                        RecordModel(
                                record.id,
                                score,
                                time,
                                note,
                        )
                )

                Toast.makeText(this, "Record updated.", Toast.LENGTH_LONG).show()
                setUpRecordList()
                setUpAverageMonthScore()
                updateDialog.dismiss()

            } else {
                Toast.makeText(this, "Mood can't be blank.", Toast.LENGTH_LONG)
                        .show()
            }

            dbHandlerRecord.close()

        }

        updateDialog.show()

        bindingUpdateRecord.tvCancelUpdateRecord.setOnClickListener {
            updateDialog.dismiss()
        }

    }

    fun deleteRecord(record: RecordModel) {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        bindingDeleteRecord = DialogDeleteRecordBinding.inflate(layoutInflater)
        val view = bindingDeleteRecord.root
        deleteDialog.setContentView(view)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingDeleteRecord.tvDeleteRecord.setOnClickListener {
            val dbHandler = RecordHandler(this, null)
            dbHandler.deleteRecord(
                    RecordModel(
                            record.id,
                            0,
                            "",
                            "",
                    )
            )

            Toast.makeText(this, "Record deleted.", Toast.LENGTH_LONG).show()
            setUpRecordList()
            dbHandler.close()
            deleteDialog.dismiss()
        }

        bindingDeleteRecord.tvCancelDeleteRecord.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }

    fun viewNoteForRecordId(recordId: Int) {
        val viewNoteDialog = Dialog(this, R.style.Theme_Dialog)
        viewNoteDialog.setCancelable(false)
        bindingViewNote = DialogViewNoteBinding.inflate(layoutInflater)
        val view = bindingViewNote.root
        viewNoteDialog.setContentView(view)
        viewNoteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dbHandler = RecordHandler(this, null)
        val noteBody = dbHandler.getNoteForId(recordId)
        dbHandler.close()

        bindingViewNote.tvNoteBody.text = noteBody

        bindingViewNote.tvDoneViewNote.setOnClickListener {
            viewNoteDialog.dismiss()
        }

        viewNoteDialog.show()
    }

    fun newerRecordOnDay(record: RecordModel): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = record.time.toLong()

        var newerRecord = false

        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val db = RecordHandler(this, null)
        val otherRecords = db.getRecordsForDayMonthYear(day, month, year)

        for (otherRecord in otherRecords) {
            if (record.time < otherRecord.time) {
                newerRecord = true
            }
        }

        return newerRecord

    }


}