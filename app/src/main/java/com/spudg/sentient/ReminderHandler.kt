package com.spudg.sentient

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class ReminderHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SentientReminderTime.db"
        private const val TABLE_REMINDER_TIME = "reminder_time"

        private const val KEY_ID = "_id"
        private const val KEY_TIME_MS = "reminder_time"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAccountsTable =
                ("CREATE TABLE $TABLE_REMINDER_TIME($KEY_ID INTEGER PRIMARY KEY,$KEY_TIME_MS TEXT)")
        db?.execSQL(createAccountsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDER_TIME")
        onCreate(db)
    }

    fun addReminderTime(timeHour: Int, timeMinute: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, timeHour)
        cal.set(Calendar.MINUTE, timeMinute)
        if (cal.timeInMillis < Calendar.getInstance().timeInMillis) {
            cal.set(Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR + 1)
        }
        val values = ContentValues()
        values.put(KEY_ID, 1)
        values.put(KEY_TIME_MS, cal.timeInMillis)

        val db = this.writableDatabase

        if (!this.timeExists()) {
            db.insert(TABLE_REMINDER_TIME, null, values)
        } else {
            db.update(TABLE_REMINDER_TIME, values, "$KEY_ID=1", null)
        }

        db.close()

    }

    fun removeReminder() {
        val db = this.writableDatabase
        db.delete(TABLE_REMINDER_TIME, "$KEY_ID=1", null)
        db.close()
    }

    fun getReminderTime(): String {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_REMINDER_TIME",
                null
        )
        var date = ""

        if (cursor.moveToFirst()) {
            do {
                date = cursor.getString(cursor.getColumnIndex(KEY_TIME_MS))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return date

    }

    fun timeExists(): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_REMINDER_TIME",
                null
        )

        val exists = cursor.moveToFirst()

        cursor.close()

        return exists

    }

}