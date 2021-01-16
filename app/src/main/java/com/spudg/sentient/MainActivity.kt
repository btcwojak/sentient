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
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.spudg.sentient.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.recordsBtn.setOnClickListener {

        }

        binding.moodsBtn.setOnClickListener {

        }

        binding.visualiserBtn.setOnClickListener {

        }


    }

    private fun setUpRecordList() {
        if (getRecordList().size > 0) {
            binding.rvRecords.visibility = View.VISIBLE
            binding.tvNoRecords.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            binding.rvRecords.layoutManager = manager
            val policyAdapter = RecordAdapter(this, getRecordList())
            binding.rvRecords.adapter = policyAdapter
        } else {
            binding.rvRecords.visibility = View.GONE
            binding.tvNoRecords.visibility = View.VISIBLE
        }
    }

    private fun getRecordList(): ArrayList<RecordModel> {
        val dbHandler = RecordHandler(this, null)
        val result = dbHandler.filterRecords(1)
        dbHandler.close()
        return result
    }

    private fun addPolicy() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add_policy)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        addDialog.change_date_add_policy.text =
                "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"

        addDialog.change_date_add_policy.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            changeDateDialog.setContentView(R.layout.day_month_year_picker)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                changeDateDialog.dmyp_day.maxValue = 30
                changeDateDialog.dmyp_day.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                changeDateDialog.dmyp_day.maxValue = 29
                changeDateDialog.dmyp_day.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                changeDateDialog.dmyp_day.maxValue = 28
                changeDateDialog.dmyp_day.minValue = 1
            } else {
                changeDateDialog.dmyp_day.maxValue = 31
                changeDateDialog.dmyp_day.minValue = 1
            }

            changeDateDialog.dmyp_month.maxValue = 12
            changeDateDialog.dmyp_month.minValue = 1
            changeDateDialog.dmyp_year.maxValue = 2999
            changeDateDialog.dmyp_year.minValue = 1000

            changeDateDialog.dmyp_day.value = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            changeDateDialog.dmyp_month.value = Calendar.getInstance()[Calendar.MONTH] + 1
            changeDateDialog.dmyp_year.value = Calendar.getInstance()[Calendar.YEAR]
            dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
            yearPicked = Calendar.getInstance()[Calendar.YEAR]

            changeDateDialog.dmyp_month.displayedValues = Constants.MONTHS_SHORT_ARRAY

            changeDateDialog.dmyp_day.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            changeDateDialog.dmyp_month.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    changeDateDialog.dmyp_day.maxValue = 30
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 == 0)) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 != 0)) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                } else {
                    changeDateDialog.dmyp_day.maxValue = 31
                    changeDateDialog.dmyp_day.minValue = 1
                }
                monthPicked = newVal
            }

            changeDateDialog.dmyp_year.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal % 4 != 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                }
                yearPicked = newVal
            }

            changeDateDialog.submit_dmy.setOnClickListener {
                addDialog.change_date_add_policy.text =
                        "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.dmyp_day.wrapSelectorWheel = true
            changeDateDialog.dmyp_month.wrapSelectorWheel = true
            changeDateDialog.dmyp_year.wrapSelectorWheel = true

            changeDateDialog.cancel_dmy.setOnClickListener {
                dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
                monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
                yearPicked = Calendar.getInstance()[Calendar.YEAR]
                addDialog.change_date_add_policy.text =
                        "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.show()

        }


        addDialog.policy_price_layout.policy_price_et.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(arg0: Editable) {
                val str = addDialog.policy_price_layout.policy_price_et.text.toString()
                if (str.isEmpty()) return
                val str2: String = currencyInputFilter(str, 4, 2)
                if (str2 != str) {
                    addDialog.policy_price_layout.policy_price_et.setText(str2)
                    addDialog.policy_price_layout.policy_price_et.setSelection(str2.length)
                }
            }
        })

        val frequencyNames = Constants.RECURRING_FREQUENCIES
        val frequencyAdapter = ArrayAdapter(this, R.layout.custom_spinner, frequencyNames)
        addDialog.policy_frequency_spinner_add.adapter = frequencyAdapter
        addDialog.policy_frequency_spinner_add.onItemSelectedListener = this

        val tagListHandler = TagHandler(this, null)
        val items = tagListHandler.getAllTagTitles()
        tagListHandler.close()
        val tagAdapter = ArrayAdapter(this, R.layout.custom_spinner, items)
        addDialog.policy_tag_spinner_add.adapter = tagAdapter
        addDialog.policy_tag_spinner_add.onItemSelectedListener = this

        addDialog.tvAddPolicy.setOnClickListener {

            val dbHandlerPolicies = PolicyHandler(this, null)
            val dbHandlerTags = TagHandler(this, null)

            // For request code
            val minuteForRC = Calendar.getInstance()[Calendar.MINUTE]
            val hourForRC = Calendar.getInstance()[Calendar.HOUR]
            val dayForRC = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
            val yearForRC = Calendar.getInstance()[Calendar.YEAR].toString().takeLast(2)

            val tag = dbHandlerTags.getTagId(selectedTag)
            val price = addDialog.policy_price_layout.policy_price_et.text.toString()
            val note = addDialog.etNoteLayoutAddPolicy.etNoteAddPolicy.text.toString()
            val nextMonth = monthPicked
            val notifRC = "$minuteForRC$hourForRC$dayForRC$yearForRC".toInt()
            val nextDay = dayPicked
            val nextYear = yearPicked
            val frequency = selectedFrequency

            if (selectedFrequency.isNotEmpty() && price.isNotEmpty() && note.isNotEmpty()) {
                dbHandlerPolicies.addPolicy(
                        PolicyModel(
                                0,
                                note,
                                tag,
                                price,
                                nextMonth,
                                notifRC,
                                nextDay,
                                nextYear,
                                "",
                                frequency
                        )
                )

                // Notification setting
                val strDate = "${nextDay}-${nextMonth}-${nextYear}"
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val nextDateMillis = sdf.parse(strDate)?.time

                val intent = Intent(this, PolicyCheckReminder::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, notifRC, intent, 0)
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        nextDateMillis!! - 86400000,
                        pendingIntent
                )

                Toast.makeText(this, "Policy added.", Toast.LENGTH_LONG).show()
                setUpPolicyList()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Price or note can't be blank.", Toast.LENGTH_LONG)
                        .show()
            }

            dbHandlerPolicies.close()

        }
        addDialog.show()

        addDialog.tvCancelAddPolicy.setOnClickListener {
            addDialog.dismiss()
        }
    }

    fun updatePolicy(policy: PolicyModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        updateDialog.setContentView(R.layout.dialog_update_policy)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = policy.nextDay
        var monthPicked = policy.nextMonth
        var yearPicked = policy.nextYear

        updateDialog.change_date_update_policy.text =
                "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"

        updateDialog.change_date_update_policy.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            changeDateDialog.setContentView(R.layout.day_month_year_picker)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (policy.nextMonth == 4 || policy.nextMonth == 6 || policy.nextMonth == 9 || policy.nextMonth == 11) {
                changeDateDialog.dmyp_day.maxValue = 30
                changeDateDialog.dmyp_day.minValue = 1
            } else if (policy.nextMonth == 2 && policy.nextMonth % 4 == 0) {
                changeDateDialog.dmyp_day.maxValue = 29
                changeDateDialog.dmyp_day.minValue = 1
            } else if (policy.nextMonth == 2 && policy.nextMonth % 4 != 0) {
                changeDateDialog.dmyp_day.maxValue = 28
                changeDateDialog.dmyp_day.minValue = 1
            } else {
                changeDateDialog.dmyp_day.maxValue = 31
                changeDateDialog.dmyp_day.minValue = 1
            }

            changeDateDialog.dmyp_month.maxValue = 12
            changeDateDialog.dmyp_month.minValue = 1
            changeDateDialog.dmyp_year.maxValue = 2999
            changeDateDialog.dmyp_year.minValue = 1000

            changeDateDialog.dmyp_day.value = policy.nextDay
            changeDateDialog.dmyp_month.value = policy.nextMonth
            changeDateDialog.dmyp_year.value = policy.nextYear
            dayPicked = policy.nextDay
            monthPicked = policy.nextMonth
            yearPicked = policy.nextYear

            changeDateDialog.dmyp_month.displayedValues = Constants.MONTHS_SHORT_ARRAY

            changeDateDialog.dmyp_day.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            changeDateDialog.dmyp_month.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    changeDateDialog.dmyp_day.maxValue = 30
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 == 0)) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 != 0)) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                } else {
                    changeDateDialog.dmyp_day.maxValue = 31
                    changeDateDialog.dmyp_day.minValue = 1
                }
                monthPicked = newVal
            }

            changeDateDialog.dmyp_year.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal % 4 != 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                }
                yearPicked = newVal
            }

            changeDateDialog.submit_dmy.setOnClickListener {
                updateDialog.change_date_update_policy.text =
                        "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }



            changeDateDialog.dmyp_day.wrapSelectorWheel = true
            changeDateDialog.dmyp_month.wrapSelectorWheel = true
            changeDateDialog.dmyp_year.wrapSelectorWheel = true

            changeDateDialog.cancel_dmy.setOnClickListener {
                dayPicked = policy.nextDay
                monthPicked = policy.nextMonth
                yearPicked = policy.nextYear
                updateDialog.change_date_update_policy.text =
                        "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.show()

        }

        updateDialog.policy_price_layout_update.policy_price_et_update.addTextChangedListener(object :
                TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(arg0: Editable) {
                val str =
                        updateDialog.policy_price_layout_update.policy_price_et_update.text.toString()
                if (str.isEmpty()) return
                val str2: String = currencyInputFilter(str, 4, 2)
                if (str2 != str) {
                    updateDialog.policy_price_layout_update.policy_price_et_update.setText(str2)
                    updateDialog.policy_price_layout_update.policy_price_et_update.setSelection(str2.length)
                }
            }
        })

        val frequencies = Constants.RECURRING_FREQUENCIES
        val frequencyAdapter = ArrayAdapter(this, R.layout.custom_spinner, frequencies)
        updateDialog.policy_frequency_spinner_update.adapter = frequencyAdapter
        updateDialog.policy_frequency_spinner_update.onItemSelectedListener = this
        val freqId = when (policy.frequency) {
            "Weekly" -> 1
            "Bi-weekly" -> 2
            "Tri-weekly" -> 3
            "Four-weekly" -> 4
            "Monthly" -> 5
            "Bi-monthly" -> 6
            "Quarterly" -> 7
            "Yearly" -> 8
            else -> 1
        }

        updateDialog.policy_frequency_spinner_update.setSelection(freqId - 1)

        val tagListHandler = TagHandler(this, null)
        val items = tagListHandler.getAllTagTitles()
        tagListHandler.close()
        val tagAdapter = ArrayAdapter(this, R.layout.custom_spinner, items)
        updateDialog.policy_tag_spinner_update.adapter = tagAdapter
        updateDialog.policy_tag_spinner_update.onItemSelectedListener = this
        updateDialog.policy_tag_spinner_update.setSelection(policy.tag - 1)

        updateDialog.policy_price_layout_update.policy_price_et_update.setText(policy.price)

        updateDialog.etNoteLayoutUpdatePolicy.etNoteUpdatePolicy.setText(policy.note)

        updateDialog.tvUpdatePolicy.setOnClickListener {
            val dbHandlerPolicies = PolicyHandler(this, null)
            val dbHandlerTags = TagHandler(this, null)

            // Remove old notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(policy.notifRC)

            // For request code
            val minuteForRC = Calendar.getInstance()[Calendar.MINUTE]
            val hourForRC = Calendar.getInstance()[Calendar.HOUR]
            val dayForRC = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
            val yearForRC = Calendar.getInstance()[Calendar.YEAR].toString().takeLast(2)

            val tag = dbHandlerTags.getTagId(selectedTag)
            val price =
                    updateDialog.policy_price_layout_update.policy_price_et_update.text.toString()
            val note = updateDialog.etNoteLayoutUpdatePolicy.etNoteUpdatePolicy.text.toString()
            val nextMonth = monthPicked
            val notifRC = "$minuteForRC$hourForRC$dayForRC$yearForRC".toInt()
            val nextDay = dayPicked
            val nextYear = yearPicked
            val frequency = selectedFrequency

            if (selectedFrequency.isNotEmpty() && price.isNotEmpty() && note.isNotEmpty()) {
                dbHandlerPolicies.updatePolicy(
                        PolicyModel(
                                policy.id,
                                note,
                                tag,
                                price,
                                nextMonth,
                                notifRC,
                                nextDay,
                                nextYear,
                                "",
                                frequency
                        )
                )

                // New notification setting
                val strDate = "${nextDay}-${nextMonth}-${nextYear}"
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val nextDateMillis = sdf.parse(strDate)?.time

                val intent = Intent(this, PolicyCheckReminder::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, notifRC, intent, 0)
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        nextDateMillis!! - 86400000,
                        pendingIntent
                )

                Toast.makeText(this, "Policy updated.", Toast.LENGTH_LONG).show()
                setUpPolicyList()
                updateDialog.dismiss()

            } else {
                Toast.makeText(
                        this,
                        "Tag, price, frequency or note can't be blank.",
                        Toast.LENGTH_LONG
                )
                        .show()
            }

            dbHandlerPolicies.close()
            dbHandlerTags.close()

        }

        updateDialog.tvCancelUpdatePolicy.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    fun deletePolicy(policy: PolicyModel) {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        deleteDialog.setContentView(R.layout.dialog_delete_policy)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Remove old notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(policy.notifRC)

        deleteDialog.tvDeletePolicy.setOnClickListener {
            val dbHandler = PolicyHandler(this, null)
            dbHandler.deletePolicy(
                    PolicyModel(
                            policy.id,
                            "",
                            0,
                            "",
                            0,
                            0,
                            0,
                            0,
                            "",
                            ""
                    )
            )

            Toast.makeText(this, "Policy deleted.", Toast.LENGTH_LONG).show()
            setUpPolicyList()
            dbHandler.close()
            deleteDialog.dismiss()
        }

        deleteDialog.tvCancelDeletePolicy.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }






}