package com.spudg.sentient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.spudg.sentient.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {

    private lateinit var bindingLanding: ActivityLandingBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingLanding = ActivityLandingBinding.inflate(layoutInflater)
        val view = bindingLanding.root
        setContentView(view)

        bindingLanding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        bindingLanding.btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

    }

}