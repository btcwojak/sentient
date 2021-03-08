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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spudg.sentient.databinding.*

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var bindingAccount: ActivityAccountBinding
    private lateinit var bindingEmailVerification: DialogResentEmailVerificationBinding
    private lateinit var bindingReAuthenticate: DialogReauthenticateBinding
    private lateinit var bindingChangeName: DialogChangeNameBinding
    private lateinit var bindingChangeEmail: DialogChangeEmailBinding
    private lateinit var bindingChangePassword: DialogChangePasswordBinding

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

        bindingAccount.btnChangeName.setOnClickListener {
            val changeNameDialog = Dialog(this, R.style.Theme_Dialog)
            changeNameDialog.setCancelable(false)
            bindingChangeName = DialogChangeNameBinding.inflate(layoutInflater)
            val view = bindingChangeName.root
            changeNameDialog.setContentView(view)
            changeNameDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            bindingChangeName.tvChangeNameSubmit.setOnClickListener {
                if (bindingChangeName.etNewName.text.toString().isNotEmpty()) {
                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(bindingChangeName.etNewName.text.toString()).build()
                    auth.currentUser!!.updateProfile(profileUpdates)
                    Toast.makeText(this, "All done - reload this page for your name to update.", Toast.LENGTH_SHORT).show()
                    changeNameDialog.dismiss()
                } else {
                    Toast.makeText(this, "Your new name can't be blank.", Toast.LENGTH_SHORT).show()
                }

            }
            bindingChangeName.tvCancelChangeName.setOnClickListener {
                changeNameDialog.dismiss()
            }
            changeNameDialog.show()
        }

        bindingAccount.btnChangeEmail.setOnClickListener {
            val reAuthenticateDialog = Dialog(this, R.style.Theme_Dialog)
            reAuthenticateDialog.setCancelable(false)
            bindingReAuthenticate = DialogReauthenticateBinding.inflate(layoutInflater)
            val view = bindingReAuthenticate.root
            reAuthenticateDialog.setContentView(view)
            reAuthenticateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingReAuthenticate.tvSubmit.setOnClickListener {
                val user = Firebase.auth.currentUser!!
                if (bindingReAuthenticate.etEmail.text.toString().isNotEmpty() && bindingReAuthenticate.etPassword.text.toString().isNotEmpty()) {
                    val credential = EmailAuthProvider
                            .getCredential(bindingReAuthenticate.etEmail.text.toString(), bindingReAuthenticate.etPassword.text.toString())
                    user.reauthenticate(credential)
                            .addOnSuccessListener {
                                Log.e("re-auth", "User re-authenticated.")
                                val changeEmailBinding = Dialog(this, R.style.Theme_Dialog)
                                changeEmailBinding.setCancelable(false)
                                bindingChangeEmail = DialogChangeEmailBinding.inflate(layoutInflater)
                                val viewCE = bindingChangeEmail.root
                                changeEmailBinding.setContentView(viewCE)
                                changeEmailBinding.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                bindingChangeEmail.tvChangeEmail.setOnClickListener {
                                    user.updateEmail(bindingChangeEmail.etNewEmail.text.toString()).addOnSuccessListener {
                                        Toast.makeText(this, "Email changed successfully.", Toast.LENGTH_SHORT).show()
                                        changeEmailBinding.dismiss()
                                        reAuthenticateDialog.dismiss()
                                    } .addOnFailureListener {
                                        Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show()
                                        changeEmailBinding.dismiss()
                                        reAuthenticateDialog.dismiss()
                                    }
                                }
                                bindingChangeEmail.tvCancelChangeEmail.setOnClickListener {
                                    changeEmailBinding.dismiss()
                                }
                                changeEmailBinding.show()
                            }

                            .addOnFailureListener {
                                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                                Log.e("re-auth", "User not re-authenticated.")}
                } else {
                    Toast.makeText(this, "Email or password can't be blank.", Toast.LENGTH_SHORT).show()
                }

            }

            bindingReAuthenticate.tvCancel.setOnClickListener {
                reAuthenticateDialog.dismiss()
            }

            reAuthenticateDialog.show()
        }

        bindingAccount.btnChangePassword.setOnClickListener {
            val reAuthenticateDialog = Dialog(this, R.style.Theme_Dialog)
            reAuthenticateDialog.setCancelable(false)
            bindingReAuthenticate = DialogReauthenticateBinding.inflate(layoutInflater)
            val view = bindingReAuthenticate.root
            reAuthenticateDialog.setContentView(view)
            reAuthenticateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingReAuthenticate.tvSubmit.setOnClickListener {
                if (bindingReAuthenticate.etEmail.text.toString().isNotEmpty() && bindingReAuthenticate.etPassword.text.toString().isNotEmpty()) {
                    val user = Firebase.auth.currentUser!!
                    val credential = EmailAuthProvider
                            .getCredential(bindingReAuthenticate.etEmail.text.toString(), bindingReAuthenticate.etPassword.text.toString())
                    user.reauthenticate(credential)
                            .addOnSuccessListener {
                                Log.e("re-auth", "User re-authenticated.")
                                val changePasswordDialog = Dialog(this, R.style.Theme_Dialog)
                                changePasswordDialog.setCancelable(false)
                                bindingChangePassword = DialogChangePasswordBinding.inflate(layoutInflater)
                                val viewCP = bindingChangePassword.root
                                changePasswordDialog.setContentView(viewCP)
                                changePasswordDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                bindingChangePassword.tvChangePassword.setOnClickListener {
                                    user.updatePassword(bindingChangePassword.etNewPassword.text.toString()).addOnSuccessListener {
                                        Toast.makeText(this, "Password changed successfully.", Toast.LENGTH_SHORT).show()
                                        changePasswordDialog.dismiss()
                                        reAuthenticateDialog.dismiss()
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show()
                                        changePasswordDialog.dismiss()
                                        reAuthenticateDialog.dismiss()
                                    }
                                }
                                bindingChangePassword.tvCancelChangePassword.setOnClickListener {
                                    changePasswordDialog.dismiss()
                                }
                                changePasswordDialog.show()
                            }

                            .addOnFailureListener {
                                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                                Log.e("re-auth", "User not re-authenticated.")
                            }
                } else {
                    Toast.makeText(this, "Email or password can't be blank.", Toast.LENGTH_SHORT).show()
                }
            }

            bindingReAuthenticate.tvCancel.setOnClickListener {
                reAuthenticateDialog.dismiss()
            }

            reAuthenticateDialog.show()
        }

        bindingAccount.userEmail.text = "Email: ${auth.currentUser?.email}"

        val reference = database.ref.child("users").child(auth.currentUser!!.uid).child("records")
        reference.keepSynced(true)

        val numberRecordsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bindingAccount.totalRecordsPosted.text = "Total records posted: " + snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("test", "Error getting data", error.toException())
            }

        }

        reference.addValueEventListener(numberRecordsListener)

    }

}