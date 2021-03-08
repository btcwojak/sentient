package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.ActivitySignInBinding
import com.spudg.sentient.databinding.DialogForgotPasswordBinding
import com.spudg.sentient.databinding.DialogReminderBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var signInBinding: ActivitySignInBinding
    private lateinit var forgotPasswordBinding: DialogForgotPasswordBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signInBinding = ActivitySignInBinding.inflate(layoutInflater)
        val view = signInBinding.root
        setContentView(view)

        auth = Firebase.auth

        signInBinding.btnForgotPassword.setOnClickListener {
            forgotPassword()
        }

        signInBinding.btnBack.setOnClickListener {
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish()
        }

        signInBinding.btnSignIn.setOnClickListener {
            if (signInBinding.email.text.toString().isNotEmpty() && signInBinding.password.text.toString().isNotEmpty()) {
                submitLogInInfo(signInBinding.email.text.toString(), signInBinding.password.text.toString())
            } else {
                Toast.makeText(this, "Email or password can't be blank", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun forgotPassword() {
        val forgotPasswordDialog = Dialog(this, R.style.Theme_Dialog)
        forgotPasswordDialog.setCancelable(false)
        forgotPasswordBinding = DialogForgotPasswordBinding.inflate(layoutInflater)
        val view = forgotPasswordBinding.root
        forgotPasswordDialog.setContentView(view)
        forgotPasswordDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        forgotPasswordBinding.tvSendResetEmail.setOnClickListener {
            val email = forgotPasswordBinding.etForgotPassword.text.toString()

            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                Toast.makeText(this, "An email has been sent to reset your password.", Toast.LENGTH_SHORT).show()
                forgotPasswordDialog.dismiss()
            } .addOnFailureListener {
                Toast.makeText(this, "That email is not recognised. Try signing up for a new account.", Toast.LENGTH_SHORT).show()
            }
        }

        forgotPasswordBinding.tvCancelResetPassword.setOnClickListener {
            forgotPasswordDialog.dismiss()
        }

        forgotPasswordDialog.show()
    }

    private fun submitLogInInfo(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LogIn", "signInWithEmail:success")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.w("LogIn", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, task.exception.toString(),
                                Toast.LENGTH_LONG).show()
                    }

                }
    }


}