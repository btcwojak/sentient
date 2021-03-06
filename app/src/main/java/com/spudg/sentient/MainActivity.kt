package com.spudg.sentient

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.*
import nl.dionsegijn.konfetti.models.Shape
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingAddRecord: DialogAddRecordBinding
    private lateinit var bindingUpdateRecord: DialogUpdateRecordBinding
    private lateinit var bindingDeleteRecord: DialogDeleteRecordBinding
    private lateinit var bindingDMYP: DayMonthYearPickerBinding
    private lateinit var bindingHMP: HourMinutePickerBinding
    private lateinit var bindingViewNote: DialogViewNoteBinding
    private lateinit var bindingReminder: DialogReminderBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)

        bindingMain.recordRowShimmer.visibility = View.VISIBLE
        bindingMain.topSectionShimmer.visibility = View.VISIBLE
        bindingMain.recordRowShimmer.startShimmerAnimation()
        bindingMain.topSectionShimmer.startShimmerAnimation()
        bindingMain.rvRecords.visibility = View.GONE
        bindingMain.averageScoreMonth.visibility = View.GONE
        bindingMain.monthHeading.visibility = View.GONE
        bindingMain.averageScoreMonthText.visibility = View.GONE

        auth = Firebase.auth
        database = Firebase.database.reference

        createNotificationChannel()

        bindingMain.visualiserBtn.setOnClickListener {
            val intent = Intent(this, VisualiserActivity::class.java)
            startActivity(intent)
        }

        bindingMain.reminderBtn.setOnClickListener {
            openReminderDialog()
        }

        bindingMain.moreBtn.setOnClickListener {
            val popupMenu = PopupMenu(this, bindingMain.moreBtn)
            popupMenu.menuInflater.inflate(R.menu.menu_popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_account -> {
                        val intent = Intent(this, AccountActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.action_about -> {
                        val intent = Intent(this, AboutActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.action_logout -> {
                        Firebase.auth.signOut()
                        val intent = Intent(this, LandingActivity::class.java)
                        startActivity(intent)
                    }
                }
                true
            }

            popupMenu.show()
        }

        bindingMain.addRecord.setOnClickListener {
            addRecord()
        }

        setUpRecordList()
        setUpAverageMonthScore()

    }

    private fun openReminderDialog() {
        val addUpdateReminderDialog = Dialog(this, R.style.Theme_Dialog)
        addUpdateReminderDialog.setCancelable(false)
        bindingReminder = DialogReminderBinding.inflate(layoutInflater)
        val view = bindingReminder.root
        addUpdateReminderDialog.setContentView(view)
        addUpdateReminderDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val db = ReminderHandler(this, null)

        if (db.timeExists()) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = db.getReminderTime().toLong()
            var timeHour = cal.get(Calendar.HOUR_OF_DAY)
            var timeMinute = cal.get(Calendar.MINUTE)

            setButtonsForExistingReminder(timeHour, timeMinute)

            bindingReminder.btnAddUpdateReminder.setOnClickListener {
                val updateTimeDialog = Dialog(this, R.style.Theme_Dialog)
                updateTimeDialog.setCancelable(false)
                bindingHMP = HourMinutePickerBinding.inflate(layoutInflater)
                val viewHMP = bindingHMP.root
                updateTimeDialog.setContentView(viewHMP)
                updateTimeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                bindingHMP.hourMinutePickerTitle.text = getString(R.string.set_daily_reminder_time)

                bindingHMP.dmypHour.setFormatter { i -> String.format("%02d", i) }
                bindingHMP.dmypMinute.setFormatter { i -> String.format("%02d", i) }

                bindingHMP.dmypHour.maxValue = 23
                bindingHMP.dmypHour.minValue = 0
                bindingHMP.dmypMinute.maxValue = 59
                bindingHMP.dmypMinute.minValue = 0

                bindingHMP.dmypHour.value = timeHour
                bindingHMP.dmypMinute.value = timeMinute

                bindingHMP.dmypHour.setOnValueChangedListener { _, _, newVal ->
                    timeHour = newVal
                }

                bindingHMP.dmypMinute.setOnValueChangedListener { _, _, newVal ->
                    timeMinute = newVal
                }

                bindingHMP.submitHm.setOnClickListener {
                    val dbSubmit = ReminderHandler(this, null)
                    dbSubmit.addReminderTime(timeHour, timeMinute)

                    val alarmIntent = Intent(applicationContext, RecordReminder::class.java)
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    val displayIntent =
                        PendingIntent.getBroadcast(applicationContext, 1, alarmIntent, 0)
                    alarmManager.cancel(displayIntent)

                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            db.getReminderTime().toLong(),
                            86400000,
                            displayIntent
                    )

                    setButtonsForExistingReminder(timeHour, timeMinute)

                    updateTimeDialog.dismiss()
                }

                bindingHMP.dmypHour.wrapSelectorWheel = true
                bindingHMP.dmypMinute.wrapSelectorWheel = true

                updateTimeDialog.show()
            }

            bindingReminder.btnRemoveReminder.setOnClickListener {
                val alarmIntent = Intent(applicationContext, RecordReminder::class.java)
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val displayIntent =
                    PendingIntent.getBroadcast(applicationContext, 1, alarmIntent, 0)
                alarmManager.cancel(displayIntent)

                db.removeReminder()

                setButtonsForNoReminder()

                bindingReminder.btnRemoveReminder.visibility = View.GONE
            }

        } else {

            setButtonsForNoReminder()

            bindingReminder.btnAddUpdateReminder.setOnClickListener {
                var timeHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                var timeMinute = Calendar.getInstance().get(Calendar.MINUTE)
                val updateTimeDialog = Dialog(this, R.style.Theme_Dialog)
                updateTimeDialog.setCancelable(false)
                bindingHMP = HourMinutePickerBinding.inflate(layoutInflater)
                val viewHMP = bindingHMP.root
                updateTimeDialog.setContentView(viewHMP)
                updateTimeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                bindingHMP.hourMinutePickerTitle.text = getString(R.string.set_daily_reminder_time)

                bindingHMP.dmypHour.setFormatter { i -> String.format("%02d", i) }
                bindingHMP.dmypMinute.setFormatter { i -> String.format("%02d", i) }

                bindingHMP.dmypHour.maxValue = 23
                bindingHMP.dmypHour.minValue = 0
                bindingHMP.dmypMinute.maxValue = 59
                bindingHMP.dmypMinute.minValue = 0

                bindingHMP.dmypHour.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                bindingHMP.dmypMinute.value = Calendar.getInstance().get(Calendar.MINUTE)

                bindingHMP.dmypHour.setOnValueChangedListener { _, _, newVal ->
                    timeHour = newVal
                }

                bindingHMP.dmypMinute.setOnValueChangedListener { _, _, newVal ->
                    timeMinute = newVal
                }

                bindingHMP.submitHm.setOnClickListener {
                    setButtonsForExistingReminder(timeHour, timeMinute)
                    val dbSubmit = ReminderHandler(this, null)
                    dbSubmit.addReminderTime(timeHour, timeMinute)

                    val alarmIntent = Intent(applicationContext, RecordReminder::class.java)
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    val displayIntent =
                        PendingIntent.getBroadcast(applicationContext, 1, alarmIntent, 0)
                    alarmManager.cancel(displayIntent)

                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            db.getReminderTime().toLong(),
                            86400000,
                            displayIntent
                    )

                    setButtonsForExistingReminder(timeHour, timeMinute)

                    updateTimeDialog.dismiss()
                }

                bindingHMP.dmypHour.wrapSelectorWheel = true
                bindingHMP.dmypMinute.wrapSelectorWheel = true

                updateTimeDialog.show()
            }

            bindingReminder.btnRemoveReminder.setOnClickListener {
                val alarmIntent = Intent(applicationContext, RecordReminder::class.java)
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val displayIntent =
                    PendingIntent.getBroadcast(applicationContext, 1, alarmIntent, 0)
                alarmManager.cancel(displayIntent)

                db.removeReminder()

                setButtonsForNoReminder()
            }

        }

        bindingReminder.tvDoneReminder.setOnClickListener {
            addUpdateReminderDialog.dismiss()
        }

        addUpdateReminderDialog.show()

    }

    private fun setButtonsForExistingReminder(timeHour: Int, timeMinute: Int) {
        bindingReminder.currentTime.text = getString(
                R.string.current_reminder_time,
                String.format("%02d", timeHour),
                String.format("%02d", timeMinute)
        )
        bindingReminder.btnAddUpdateReminder.text = getString(R.string.update_your_reminder)
        bindingReminder.btnRemoveReminder.visibility = View.VISIBLE

    }

    private fun setButtonsForNoReminder() {
        bindingReminder.btnAddUpdateReminder.text = getString(R.string.add_a_new_reminder)
        bindingReminder.currentTime.text = getString(R.string.current_reminder_none)
        bindingReminder.btnRemoveReminder.visibility = View.GONE
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "Daily Record Reminder"
            val description =
                "Channel to remind users to post a record for the day"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("recordReminder", name, importance)
            channel.description = description

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

        }
    }

    private fun setUpAverageMonthScore() {
        val reference = database.ref.child("users").child(auth.currentUser!!.uid).child("records")

        val snapshotRecords = ArrayList<DataSnapshot>()

        reference.get().addOnSuccessListener { dataSnapshot ->

            val allRecords = ArrayList<RecordModel>()

            for (record in dataSnapshot.children) {
                snapshotRecords.add(record)
            }

            repeat(snapshotRecords.size) {
                val id = snapshotRecords[it].key.toString()
                val note = snapshotRecords[it].child("note").value.toString()
                val score = snapshotRecords[it].child("score").value.toString().toInt()
                val time = snapshotRecords[it].child("time").value.toString()
                allRecords.add(RecordModel(id, score, time, note))
            }

            var runningTotal = 0
            var numberOfRatings = 0

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentDate = currentMonth.toString() + currentYear.toString()

            bindingMain.monthHeading.text = getString(
                    R.string.average_month_score_heading,
                    Globals.getLongMonth(currentMonth),
                    currentYear.toString()
            )

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

                val averageScore = (runningTotal / numberOfRatings)
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
            } else {
                bindingMain.averageScoreMonth.text = "n/a"
                bindingMain.averageScoreMonth.setTextColor(Color.GRAY)
            }



        } .addOnFailureListener{
            Log.e("test", "Error getting data", it)
        }


    }

    private fun setUpRecordList() {
        val reference = database.ref.child("users").child(auth.currentUser!!.uid).child("records")
        reference.keepSynced(true)

        val snapshotRecords = ArrayList<DataSnapshot>()

        reference.get().addOnSuccessListener { dataSnapshot ->

            val records = ArrayList<RecordModel>()

            for (record in dataSnapshot.children) {
                snapshotRecords.add(record)
            }

            repeat(snapshotRecords.size) {
                val id = snapshotRecords[it].key.toString()
                val note = snapshotRecords[it].child("note").value.toString()
                val score = snapshotRecords[it].child("score").value.toString().toInt()
                val time = snapshotRecords[it].child("time").value.toString()
                records.add(RecordModel(id, score, time, note))
            }

            records.sortByDescending { it.time }

            if (records.size > 0) {
                bindingMain.rvRecords.visibility = View.VISIBLE
                bindingMain.tvNoRecords.visibility = View.GONE
                bindingMain.averageScoreMonth.visibility = View.VISIBLE
                bindingMain.averageScoreMonthText.visibility = View.VISIBLE
                bindingMain.monthHeading.visibility = View.VISIBLE
                bindingMain.recordRowShimmer.visibility = View.GONE
                bindingMain.topSectionShimmer.visibility = View.GONE
                bindingMain.recordRowShimmer.stopShimmerAnimation()
                val manager = LinearLayoutManager(this)
                bindingMain.rvRecords.layoutManager = manager
                val policyAdapter = RecordAdapter(this, records)
                bindingMain.rvRecords.adapter = policyAdapter
            } else {
                bindingMain.rvRecords.visibility = View.GONE
                bindingMain.tvNoRecords.visibility = View.VISIBLE
                bindingMain.averageScoreMonth.visibility = View.VISIBLE
                bindingMain.recordRowShimmer.visibility = View.GONE
                bindingMain.monthHeading.visibility = View.VISIBLE
                bindingMain.topSectionShimmer.visibility = View.GONE
                bindingMain.averageScoreMonthText.visibility = View.VISIBLE
                bindingMain.recordRowShimmer.stopShimmerAnimation()
            }

        }.addOnFailureListener{
            Log.e("test", "Error getting data", it)
        }

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
            getString(
                    R.string.day_month_year,
                    dayPicked.toString(),
                    Globals.getShortMonth(monthPicked),
                    yearPicked.toString()
            )

        bindingAddRecord.dateRecordPost.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYP = DayMonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingDMYP.root
            changeDateDialog.setContentView(viewDMYP)
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

            bindingDMYP.dmypDay.value = dayPicked
            bindingDMYP.dmypMonth.value = monthPicked
            bindingDMYP.dmypYear.value = yearPicked

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
                    getString(
                            R.string.day_month_year,
                            dayPicked.toString(),
                            Globals.getShortMonth(monthPicked),
                            yearPicked.toString()
                    )
                changeDateDialog.dismiss()
            }

            bindingDMYP.dmypDay.wrapSelectorWheel = true
            bindingDMYP.dmypMonth.wrapSelectorWheel = true
            bindingDMYP.dmypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        var hourPicked = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        var minutePicked = Calendar.getInstance()[Calendar.MINUTE]

        bindingAddRecord.timeRecordPost.text =
            getString(
                    R.string.hour_minute,
                    String.format("%02d", hourPicked),
                    String.format("%02d", minutePicked)
            )

        bindingAddRecord.timeRecordPost.setOnClickListener {
            val changeTimeDialog = Dialog(this, R.style.Theme_Dialog)
            changeTimeDialog.setCancelable(false)
            bindingHMP = HourMinutePickerBinding.inflate(layoutInflater)
            val viewHMP = bindingHMP.root
            changeTimeDialog.setContentView(viewHMP)
            changeTimeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingHMP.dmypHour.setFormatter { i -> String.format("%02d", i) }
            bindingHMP.dmypMinute.setFormatter { i -> String.format("%02d", i) }

            bindingHMP.dmypHour.maxValue = 23
            bindingHMP.dmypHour.minValue = 0
            bindingHMP.dmypMinute.maxValue = 59
            bindingHMP.dmypMinute.minValue = 0

            bindingHMP.dmypHour.value = hourPicked
            bindingHMP.dmypMinute.value = minutePicked

            bindingHMP.dmypHour.setOnValueChangedListener { _, _, newVal ->
                hourPicked = newVal
            }

            bindingHMP.dmypMinute.setOnValueChangedListener { _, _, newVal ->
                minutePicked = newVal
            }

            bindingHMP.submitHm.setOnClickListener {
                bindingAddRecord.timeRecordPost.text =
                    getString(
                            R.string.hour_minute,
                            String.format("%02d", hourPicked),
                            String.format("%02d", minutePicked)
                    )
                changeTimeDialog.dismiss()
            }

            bindingHMP.dmypHour.wrapSelectorWheel = true
            bindingHMP.dmypMinute.wrapSelectorWheel = true

            changeTimeDialog.show()

        }

        bindingAddRecord.scoreSliderPost.value = 50F
        bindingAddRecord.currentScorePost.text =
            bindingAddRecord.scoreSliderPost.value.roundToInt().toString()
        bindingAddRecord.currentScorePost.setTextColor(-16728577)

        bindingAddRecord.scoreSliderPost.addOnChangeListener { slider, value, _ ->
            bindingAddRecord.currentScorePost.text = value.roundToInt().toString()
            when (slider.value) {
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
                in 90F..99F -> {
                    bindingAddRecord.currentScorePost.setTextColor(-6881025)
                }
                100F -> {
                    bindingAddRecord.currentScorePost.setTextColor(-6881025)
                    bindingAddRecord.viewKonfetti.build()
                            .addColors(-6881025, -16711896)
                            .setDirection(0.0, 360.0)
                            .setSpeed(3f, 6f)
                            .setFadeOutEnabled(true)
                            .setTimeToLive(600L)
                            .addShapes(Shape.Square, Shape.Circle)
                            .addSizes(nl.dionsegijn.konfetti.models.Size(12))
                            .setPosition(
                                    bindingAddRecord.viewKonfetti.x + bindingAddRecord.viewKonfetti.width / 2,
                                    bindingAddRecord.viewKonfetti.y + bindingAddRecord.viewKonfetti.height / 3
                            )
                            .burst(100)
                }
            }
        }

        bindingAddRecord.tvPostRecord.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, dayPicked, hourPicked, minutePicked)

            val score = bindingAddRecord.scoreSliderPost.value.toInt()
            val time = calendar.timeInMillis.toString()
            val note = bindingAddRecord.etNotePostRecord.text.toString()

            if (score.toString().isNotEmpty() && time.isNotEmpty()) {
                val refPush = database.ref.child("users").child(auth.currentUser!!.uid).child("records")
                val pushRefPush = refPush.push()
                pushRefPush.child("note").setValue(note)
                pushRefPush.child("score").setValue(score)
                pushRefPush.child("time").setValue(time)

                Toast.makeText(this, "Record posted.", Toast.LENGTH_LONG).show()
                setUpRecordList()
                setUpAverageMonthScore()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Mood can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

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

        val calForOldRecordTime = Calendar.getInstance()
        calForOldRecordTime.timeInMillis = record.time.toLong()
        val oldDay = calForOldRecordTime.get(Calendar.DAY_OF_MONTH)
        val oldMonth = calForOldRecordTime.get(Calendar.MONTH) + 1
        val oldYear = calForOldRecordTime.get(Calendar.YEAR)
        val oldHour = calForOldRecordTime.get(Calendar.HOUR_OF_DAY)
        val oldMinute = calForOldRecordTime.get(Calendar.MINUTE)

        var dayPicked = oldDay
        var monthPicked = oldMonth
        var yearPicked = oldYear

        bindingUpdateRecord.dateRecordUpdate.text =
            getString(
                    R.string.day_month_year,
                    dayPicked.toString(),
                    Globals.getShortMonth(monthPicked),
                    yearPicked.toString()
            )

        bindingUpdateRecord.dateRecordUpdate.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYP = DayMonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingDMYP.root
            changeDateDialog.setContentView(viewDMYP)
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

            bindingDMYP.dmypDay.value = dayPicked
            bindingDMYP.dmypMonth.value = monthPicked
            bindingDMYP.dmypYear.value = yearPicked

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
                    getString(
                            R.string.day_month_year,
                            dayPicked.toString(),
                            Globals.getShortMonth(monthPicked),
                            yearPicked.toString()
                    )
                changeDateDialog.dismiss()
            }

            bindingDMYP.dmypDay.wrapSelectorWheel = true
            bindingDMYP.dmypMonth.wrapSelectorWheel = true
            bindingDMYP.dmypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        var hourPicked = oldHour
        var minutePicked = oldMinute

        bindingUpdateRecord.timeRecordUpdate.text =
            getString(
                    R.string.hour_minute,
                    String.format("%02d", hourPicked),
                    String.format("%02d", minutePicked)
            )

        bindingUpdateRecord.timeRecordUpdate.setOnClickListener {
            val changeTimeDialog = Dialog(this, R.style.Theme_Dialog)
            changeTimeDialog.setCancelable(false)
            bindingHMP = HourMinutePickerBinding.inflate(layoutInflater)
            val viewHMP = bindingHMP.root
            changeTimeDialog.setContentView(viewHMP)
            changeTimeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingHMP.dmypHour.setFormatter { i -> String.format("%02d", i) }
            bindingHMP.dmypMinute.setFormatter { i -> String.format("%02d", i) }

            bindingHMP.dmypHour.maxValue = 23
            bindingHMP.dmypHour.minValue = 0
            bindingHMP.dmypMinute.maxValue = 59
            bindingHMP.dmypMinute.minValue = 0

            bindingHMP.dmypHour.value = hourPicked
            bindingHMP.dmypMinute.value = minutePicked

            bindingHMP.dmypHour.setOnValueChangedListener { _, _, newVal ->
                hourPicked = newVal
            }

            bindingHMP.dmypMinute.setOnValueChangedListener { _, _, newVal ->
                minutePicked = newVal
            }

            bindingHMP.submitHm.setOnClickListener {
                bindingUpdateRecord.timeRecordUpdate.text =
                    getString(
                            R.string.hour_minute,
                            String.format("%02d", hourPicked),
                            String.format("%02d", minutePicked)
                    )
                changeTimeDialog.dismiss()
            }

            bindingHMP.dmypHour.wrapSelectorWheel = true
            bindingHMP.dmypMinute.wrapSelectorWheel = true

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

        bindingUpdateRecord.scoreSliderUpdate.addOnChangeListener { slider, value, _ ->
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
                in 90F..99F -> {
                    bindingUpdateRecord.currentScoreUpdate.setTextColor(-6881025)
                }
                100F -> {
                    bindingUpdateRecord.currentScoreUpdate.setTextColor(-6881025)
                    bindingUpdateRecord.viewKonfetti.build()
                            .addColors(-6881025, -16711896)
                            .setDirection(0.0, 360.0)
                            .setSpeed(3f, 6f)
                            .setFadeOutEnabled(true)
                            .setTimeToLive(600L)
                            .addShapes(Shape.Square, Shape.Circle)
                            .addSizes(nl.dionsegijn.konfetti.models.Size(12))
                            .setPosition(
                                    bindingUpdateRecord.viewKonfetti.x + bindingUpdateRecord.viewKonfetti.width / 2,
                                    bindingUpdateRecord.viewKonfetti.y + bindingUpdateRecord.viewKonfetti.height / 3
                            )
                            .burst(100)
                }
            }
        }

        bindingUpdateRecord.tvUpdateRecord.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, dayPicked, hourPicked, minutePicked)

            val score = bindingUpdateRecord.scoreSliderUpdate.value.toInt()
            val time = calendar.timeInMillis.toString()
            val note = bindingUpdateRecord.etNoteUpdateRecord.text.toString()

            if (score.toString().isNotEmpty() && time.isNotEmpty()) {
                val refPush = database.ref.child("users").child(auth.currentUser!!.uid).child("records").child(record.id)
                refPush.child("note").setValue(note)
                refPush.child("score").setValue(score)
                refPush.child("time").setValue(time)

                Toast.makeText(this, "Record updated.", Toast.LENGTH_LONG).show()
                setUpRecordList()
                setUpAverageMonthScore()
                updateDialog.dismiss()

            } else {
                Toast.makeText(this, "Mood can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

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
            val refPush = database.ref.child("users").child(auth.currentUser!!.uid).child("records").child(record.id)
            refPush.child("note").removeValue()
            refPush.child("score").removeValue()
            refPush.child("time").removeValue()

            Toast.makeText(this, "Record deleted.", Toast.LENGTH_LONG).show()
            setUpRecordList()
            setUpAverageMonthScore()
            deleteDialog.dismiss()
        }

        bindingDeleteRecord.tvCancelDeleteRecord.setOnClickListener {
            setUpAverageMonthScore()
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }

    fun viewNoteForRecordId(recordId: String) {
        val viewNoteDialog = Dialog(this, R.style.Theme_Dialog)
        viewNoteDialog.setCancelable(false)
        bindingViewNote = DialogViewNoteBinding.inflate(layoutInflater)
        val view = bindingViewNote.root
        viewNoteDialog.setContentView(view)
        viewNoteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingViewNote.tvNoteBody.visibility = View.GONE
        bindingViewNote.noteShimmer.visibility = View.VISIBLE
        bindingViewNote.noteShimmer.startShimmerAnimation()

        var noteBody: String

        val reference = database.ref.child("users").child(auth.currentUser!!.uid).child("records").child(recordId)

        reference.get().addOnSuccessListener { dataSnapshot ->

            noteBody = dataSnapshot.child("note").value.toString()

            if (noteBody.isNotEmpty()) {
                bindingViewNote.tvNoteBody.text = noteBody
            }

            bindingViewNote.tvNoteBody.visibility = View.VISIBLE
            bindingViewNote.noteShimmer.visibility = View.GONE
            bindingViewNote.noteShimmer.stopShimmerAnimation()

        } .addOnFailureListener{
            Log.e("test", "Error getting data", it)
        }

        bindingViewNote.tvDoneViewNote.setOnClickListener {
            viewNoteDialog.dismiss()
        }

        viewNoteDialog.show()
    }

}