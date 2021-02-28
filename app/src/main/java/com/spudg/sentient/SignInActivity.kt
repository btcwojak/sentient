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
import com.spudg.sentient.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var signInBinding: ActivitySignInBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signInBinding = ActivitySignInBinding.inflate(layoutInflater)
        val view = signInBinding.root
        setContentView(view)

        auth = Firebase.auth

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