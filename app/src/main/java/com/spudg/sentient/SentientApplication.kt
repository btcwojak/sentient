package com.spudg.sentient

import android.app.Application
import com.google.firebase.database.FirebaseDatabase


class SentientApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}