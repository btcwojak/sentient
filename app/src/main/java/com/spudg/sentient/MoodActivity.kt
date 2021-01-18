package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_mood.*
import kotlinx.android.synthetic.main.dialog_update_mood.*

class MoodActivity : AppCompatActivity() {

    var moodColourSelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        back_to_records_from_moods.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setUpMoodList()

    }

    private fun getMoodList(): ArrayList<MoodModel> {
        val dbHandler = MoodHandler(this, null)
        val result = dbHandler.filterMoods()
        dbHandler.close()
        return result
    }


    private fun setUpMoodList() {
        if (getMoodList().size >= 0) {
            rvMoods.layoutManager = LinearLayoutManager(this)
            val moodsAdapter = MoodAdapter(this, getMoodList())
            rvMoods.adapter = moodsAdapter
        }
    }

    fun updateMood(mood: MoodModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        updateDialog.setContentView(R.layout.dialog_update_mood)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        updateDialog.colourPickerUpdateMood.color = mood.colour
        updateDialog.colourPickerUpdateMood.showOldCenterColor = false

        updateDialog.tvUpdateMood.setOnClickListener {
            val name = mood.name
            val icon = mood.icon
            moodColourSelected = updateDialog.colourPickerUpdateMood.color
            val colour = moodColourSelected

            val dbHandler = MoodHandler(this, null)

            if (title.isNotEmpty() && colour.toString().isNotEmpty()) {
                dbHandler.updateMood(MoodModel(mood.id, name, colour, icon))
                Toast.makeText(this, "Mood updated.", Toast.LENGTH_LONG).show()
                setUpMoodList()
                updateDialog.dismiss()
            } else {
                Toast.makeText(this, "Name or colour can't be blank.", Toast.LENGTH_LONG).show()
            }

            dbHandler.close()

        }

        updateDialog.tvCancelUpdateMood.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }












}