package com.spudg.sentient

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList

class MoodHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SentientMoods.db"
        private const val TABLE_MOODS = "moods"

        private const val KEY_ID = "_id"
        private const val KEY_NAME = "name"
        private const val KEY_COLOUR = "colour"
        private const val KEY_ICON = "icon"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createRecordTable =
                ("CREATE TABLE $TABLE_MOODS($KEY_ID INTEGER PRIMARY KEY,$KEY_NAME TEXT,$KEY_COLOUR INTEGER,$KEY_ICON INTEGER)")
        db?.execSQL(createRecordTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MOODS")
        onCreate(db)
    }

    fun addMood(mood: MoodModel) {
        val values = ContentValues()
        values.put(KEY_NAME, mood.name)
        values.put(KEY_COLOUR, mood.colour)
        values.put(KEY_ICON, mood.icon)
        val db = this.writableDatabase
        db.insert(TABLE_MOODS, null, values)
        db.close()
    }

    fun updateMood(mood: MoodModel): Int {
        val values = ContentValues()
        values.put(KEY_NAME, mood.name)
        values.put(KEY_COLOUR, mood.colour)
        values.put(KEY_ICON, mood.icon)
        val db = this.writableDatabase
        val success = db.update(TABLE_MOODS, values, KEY_ID + "=" + mood.id, null)
        db.close()
        return success
    }

    fun getAllMoodNames(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MOODS", null)

        var name: String

        if (cursor.moveToFirst()) {
            do {
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                list.add(name)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list

    }

    fun filterMoods(sortBy: Int = 0): ArrayList<MoodModel> {
        val list = ArrayList<MoodModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_MOODS",
                null
        )

        var id: Int
        var name: String
        var colour: Int
        var icon: Int

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                colour = cursor.getInt(cursor.getColumnIndex(KEY_COLOUR))
                icon = cursor.getInt(cursor.getColumnIndex(KEY_ICON))
                val mood = MoodModel(
                        id = id,
                        name = name,
                        colour = colour,
                        icon = icon,
                )
                list.add(mood)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return list

    }

    fun getMoodNameFromId(moodId: Int): String {
        val db = this.readableDatabase

        val cursor =
            db.rawQuery("SELECT * FROM $TABLE_MOODS WHERE $KEY_ID = $moodId", null)

        val name: String

        name = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex(KEY_NAME))
        } else {
            "Error"
        }

        cursor.close()
        db.close()

        return name
    }

    fun getMoodIdFromName(moodName: String): Int {
        val db = this.readableDatabase

        val cursor =
            db.rawQuery("SELECT * FROM $TABLE_MOODS WHERE $KEY_NAME = '$moodName'", null)

        val id: Int

        id = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndex(KEY_ID))
        } else {
            1
        }

        cursor.close()
        db.close()

        return id
    }









}