package com.spudg.sentient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.ActivityAccountBinding

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

        bindingAccount.backToRecordsFromAccount.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setAccountInfo()


    }

    private fun setAccountInfo() {
        bindingAccount.userName.text = "Hi, " + auth.currentUser?.displayName

        if (auth.currentUser!!.isEmailVerified) {
            bindingAccount.emailVerified.text = "Email is verified."
            bindingAccount.btnSendEmailVerification.visibility = View.GONE
        } else {
            bindingAccount.emailVerified.text = "Email not verified."
            bindingAccount.btnSendEmailVerification.visibility = View.VISIBLE
            bindingAccount.btnSendEmailVerification.setOnClickListener {
                auth.currentUser!!.sendEmailVerification()
                Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show()
            }
        }

        bindingAccount.btnChangePassword.setOnClickListener {
            auth.sendPasswordResetEmail(auth.currentUser!!.email.toString()).addOnSuccessListener {
                Toast.makeText(this, "An email has been sent to reset your password.", Toast.LENGTH_SHORT).show()
            } .addOnFailureListener {
                Toast.makeText(this, "Sorry, an error has occurred.", Toast.LENGTH_SHORT).show()
            }
        }

        bindingAccount.userEmail.text = auth.currentUser?.email

        val reference = database.ref.child("users").child(auth.currentUser!!.uid).child("records")
        reference.keepSynced(true)
        reference.get().addOnSuccessListener { dataSnapshot ->
            bindingAccount.totalRecordsPosted.text = "Total records posted: " + dataSnapshot.childrenCount.toString()
        }.addOnFailureListener {
            Log.e("test", "Error getting data", it)
        }

    }

}