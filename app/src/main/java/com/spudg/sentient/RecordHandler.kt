package com.spudg.sentient

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class RecordHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "SentientRecords.db"
        private const val TABLE_RECORDS = "records"

        private const val KEY_ID = "_id"
        private const val KEY_SCORE = "score"
        private const val KEY_TIME = "time"
        private const val KEY_NOTE = "note"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createRecordTable =
                ("CREATE TABLE $TABLE_RECORDS($KEY_ID INTEGER PRIMARY KEY,$KEY_SCORE INTEGER,$KEY_TIME TEXT,$KEY_NOTE TEXT)")
        db?.execSQL(createRecordTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        onCreate(db)
    }

    fun addRecord(record: RecordModel): Long {
        val values = ContentValues()
        values.put(KEY_SCORE, record.score)
        values.put(KEY_TIME, record.time)
        values.put(KEY_NOTE, record.note)
        val db = this.writableDatabase
        val success = db.insert(TABLE_RECORDS, null, values)
        db.close()
        return success
    }

    fun updateRecord(record: RecordModel): Int {
        val values = ContentValues()
        values.put(KEY_SCORE, record.score)
        values.put(KEY_TIME, record.time)
        values.put(KEY_NOTE, record.note)
        val db = this.writableDatabase
        val success = db.update(TABLE_RECORDS, values, KEY_ID + "=" + record.id, null)
        db.close()
        return success
    }

    fun deleteRecord(record: RecordModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_RECORDS, KEY_ID + "=" + record.id, null)
        db.close()
        return success
    }

    fun filterRecords(sortBy: Int = 0): ArrayList<RecordModel> {
        val list = ArrayList<RecordModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_RECORDS",
                null
        )

        var id: Int
        var score: Int
        var time: String
        var note: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                score = cursor.getInt(cursor.getColumnIndex(KEY_SCORE))
                time = cursor.getString(cursor.getColumnIndex(KEY_TIME))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                val record = RecordModel(
                        id = id,
                        score = score,
                        time = time,
                        note = note,
                )
                list.add(record)
            } while (cursor.moveToNext())
        }

        if (sortBy == -1) {
            list.sortByDescending {
                it.time
            }
        }

        if (sortBy == 1) {
            list.sortBy {
                it.time
            }
        }

        cursor.close()
        db.close()

        return list

    }

    fun getNoteForId(recordId: Int): String {
        var note = ""
        val db = this.readableDatabase
        val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_RECORDS WHERE $KEY_ID = $recordId",
                null
        )

        if (cursor.moveToFirst()) {
            do {
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
            } while (cursor.moveToNext())
        }

        return if (note.isNotEmpty()) {
            note
        } else {
            "There aren't any notes on this record yet. Tap on the record to edit and add a note!"
        }

    }



}