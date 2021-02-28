package com.spudg.sentient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var signUpBinding: ActivitySignUpBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = signUpBinding.root
        setContentView(view)

        auth = Firebase.auth

        signUpBinding.btnBack.setOnClickListener {
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish()
        }

        signUpBinding.btnSignUp.setOnClickListener {
            if (signUpBinding.email.text.toString().isNotEmpty() && signUpBinding.password.text.toString().isNotEmpty()) {
                if (signUpBinding.password.text.toString() == signUpBinding.passwordConfirm.text.toString()) {
                    submitSignUpInfo(signUpBinding.email.text.toString(), signUpBinding.password.text.toString())
                } else {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email or password can't be blank", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun submitSignUpInfo(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("SignUp", "createUserWithEmail:success")
                    //val user = auth.currentUser
                    //updateUI(user)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w("SignUp", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }
            }

    }

}