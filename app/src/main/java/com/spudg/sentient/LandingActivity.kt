package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.spudg.sentient.databinding.ActivityLandingBinding
import com.spudg.sentient.databinding.DialogTermsOfUseBinding

class LandingActivity : AppCompatActivity() {

    private lateinit var bindingLanding: ActivityLandingBinding

    private lateinit var bindingTOU: DialogTermsOfUseBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingLanding = ActivityLandingBinding.inflate(layoutInflater)
        val view = bindingLanding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        bindingLanding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        bindingLanding.btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        bindingLanding.btnTOU.setOnClickListener {
            bindingTOU = DialogTermsOfUseBinding.inflate(layoutInflater)
            val viewTOU = bindingTOU.root
            val termsOfUseDialog = Dialog(this, R.style.Theme_Dialog)
            termsOfUseDialog.setCancelable(false)
            termsOfUseDialog.setContentView(viewTOU)
            termsOfUseDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingTOU.tvDoneTU.setOnClickListener {
                termsOfUseDialog.dismiss()
            }

            termsOfUseDialog.show()
        }

        bindingLanding.btnPP.setOnClickListener {
            val url =
                    "https://docs.google.com/document/d/13xwVTv2-UjiIXAd9teQtx_1icbOlJUKoLGbFfVODBdU"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

    }

}