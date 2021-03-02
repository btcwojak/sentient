package com.spudg.sentient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.ActivityAccountBinding
import com.spudg.sentient.databinding.ActivityMainBinding
import com.spudg.sentient.databinding.ActivityVisualiserBinding

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var bindingAccount: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingAccount = ActivityAccountBinding.inflate(layoutInflater)
        val view = bindingAccount.root
        setContentView(view)

        auth = Firebase.auth

        database = Firebase.database.reference
        database.keepSynced(true)

        bindingAccount.backToRecordsFromAccount.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setAccountInfo()


    }

    private fun setAccountInfo() {
        bindingAccount.userName.text = auth.currentUser?.displayName
        bindingAccount.emailVerified.text = "Email verified?" + auth.currentUser?.isEmailVerified.toString()
        bindingAccount.userEmail.text = auth.currentUser?.email

        val reference = database.ref.child("users").child(auth.currentUser!!.uid).child("records")
        reference.get().addOnSuccessListener { dataSnapshot ->
            bindingAccount.totalRecordsPosted.text = "Total records posted: " + dataSnapshot.childrenCount.toString()
        } .addOnFailureListener{
            Log.e("test", "Error getting data", it)
        }

    }

}