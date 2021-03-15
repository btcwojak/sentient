package com.spudg.sentient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.ActivitySignUpBinding
import com.spudg.sentient.databinding.DialogDataConsentBinding


class SignUpActivity : AppCompatActivity() {

    private lateinit var bindingSignUp: ActivitySignUpBinding
    private lateinit var bindingDataConsent: DialogDataConsentBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingSignUp = ActivitySignUpBinding.inflate(layoutInflater)
        val view = bindingSignUp.root
        setContentView(view)

        auth = Firebase.auth
        database = Firebase.database.reference

        bindingSignUp.btnBack.setOnClickListener {
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish()
        }

        bindingSignUp.btnSignUp.setOnClickListener {
            if (bindingSignUp.email.text.toString().isNotEmpty() && bindingSignUp.password.text.toString().isNotEmpty() && bindingSignUp.name.text.toString().isNotEmpty()) {
                if (bindingSignUp.password.text.toString() == bindingSignUp.passwordConfirm.text.toString()) {
                    val dataConsentDialog = Dialog(this, R.style.Theme_Dialog)
                    dataConsentDialog.setCancelable(false)
                    bindingDataConsent = DialogDataConsentBinding.inflate(layoutInflater)
                    val viewDataConsent = bindingDataConsent.root
                    dataConsentDialog.setContentView(viewDataConsent)
                    dataConsentDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    bindingDataConsent.btnFirebasePP.movementMethod = LinkMovementMethod.getInstance()
                    bindingDataConsent.btnSentientPP.movementMethod = LinkMovementMethod.getInstance()
                    bindingDataConsent.tvAccept.setOnClickListener {
                        submitSignUpInfo(bindingSignUp.email.text.toString(), bindingSignUp.password.text.toString(), bindingSignUp.name.text.toString())
                        dataConsentDialog.dismiss()
                    }
                    bindingDataConsent.tvCancel.setOnClickListener {
                        dataConsentDialog.dismiss()
                    }
                    dataConsentDialog.show()

                } else {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Name, email or password can't be blank", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun submitSignUpInfo(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        auth.currentUser!!.sendEmailVerification()
                        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                        auth.currentUser!!.updateProfile(profileUpdates)
                        Log.d("SignUp", "createUserWithEmail:success")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.w("SignUp", "createUserWithEmail:failure", it.exception)
                        Toast.makeText(baseContext, it.exception.toString(),
                                Toast.LENGTH_LONG).show()
                    }

                }

    }

}