package com.spudg.sentient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.ActivityLandingBinding
import com.spudg.sentient.databinding.ActivityLogInBinding

class LogInActivity : AppCompatActivity() {

    private lateinit var logInBinding: ActivityLogInBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logInBinding = ActivityLogInBinding.inflate(layoutInflater)
        val view = logInBinding.root
        setContentView(view)

        auth = Firebase.auth

        logInBinding.btnLogIn.setOnClickListener {
            if (logInBinding.email.text.toString().isNotEmpty() && logInBinding.password.text.toString().isNotEmpty()) {
                submitLogInInfo(logInBinding.email.text.toString(), logInBinding.password.text.toString())
            } else {
                Toast.makeText(this, "Email or password can't be blank", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun submitLogInInfo(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LogIn", "signInWithEmail:success")
                    //val user = auth.currentUser
                    //updateUI(user)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w("LogIn", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

            }
    }





}