package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.spudg.sentient.databinding.DialogChangePasswordSuccessBinding
import com.spudg.sentient.databinding.DialogForgotPasswordBinding
import com.spudg.sentient.databinding.DialogResentEmailVerificationBinding

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var bindingAccount: ActivityAccountBinding
    private lateinit var bindingEmailVerification: DialogResentEmailVerificationBinding
    private lateinit var bindingChangePassword: DialogChangePasswordSuccessBinding

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

                val resentEmailVerificationDialog = Dialog(this, R.style.Theme_Dialog)
                resentEmailVerificationDialog.setCancelable(false)
                bindingEmailVerification = DialogResentEmailVerificationBinding.inflate(layoutInflater)
                val view = bindingEmailVerification.root
                resentEmailVerificationDialog.setContentView(view)
                resentEmailVerificationDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                bindingEmailVerification.tvDoneTU.setOnClickListener {
                    resentEmailVerificationDialog.dismiss()
                }
                resentEmailVerificationDialog.show()
            }
        }

        bindingAccount.btnChangePassword.setOnClickListener {
            auth.sendPasswordResetEmail(auth.currentUser!!.email.toString()).addOnSuccessListener {
                bindingAccount.btnChangePassword.setOnClickListener {
                    val changePasswordSuccessDialog = Dialog(this, R.style.Theme_Dialog)
                    changePasswordSuccessDialog.setCancelable(false)
                    bindingChangePassword = DialogChangePasswordSuccessBinding.inflate(layoutInflater)
                    val view = bindingChangePassword.root
                    changePasswordSuccessDialog.setContentView(view)
                    changePasswordSuccessDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    bindingChangePassword.tvDoneTU.setOnClickListener {
                        changePasswordSuccessDialog.dismiss()
                    }
                    changePasswordSuccessDialog.show()
                }
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