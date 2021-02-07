package com.spudg.sentient

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spudg.sentient.databinding.ActivityAboutBinding
import com.spudg.sentient.databinding.DialogTermsOfUseBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var bindingAbout: ActivityAboutBinding
    private lateinit var bindingTOU: DialogTermsOfUseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingAbout = ActivityAboutBinding.inflate(layoutInflater)
        val view = bindingAbout.root
        setContentView(view)

        var version = packageManager.getPackageInfo(packageName, 0).versionName
        bindingAbout.sentientDesc.text = "v$version, made by Spudg Studios"

        bindingAbout.backToRecordsFromAbout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        bindingAbout.privacyPolicy.setOnClickListener {
            privacyPolicy()
        }

        bindingAbout.termsOfUse.setOnClickListener {
            termsOfUse()
        }

        bindingAbout.rateBtn.setOnClickListener {
            rate()
        }

        bindingAbout.emailBtn.setOnClickListener {
            email()
        }

    }

    private fun privacyPolicy() {
        val url =
            "https://docs.google.com/document/d/13xwVTv2-UjiIXAd9teQtx_1icbOlJUKoLGbFfVODBdU"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun termsOfUse() {
        bindingTOU = DialogTermsOfUseBinding.inflate(layoutInflater)
        val view = bindingTOU.root
        val termsOfUseDialog = Dialog(this, R.style.Theme_Dialog)
        termsOfUseDialog.setCancelable(false)
        termsOfUseDialog.setContentView(view)
        termsOfUseDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingTOU.tvDoneTU.setOnClickListener {
            termsOfUseDialog.dismiss()
        }

        termsOfUseDialog.show()

    }

    private fun email() {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("spudgstudios@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Sentient - Suggestion / bug report")

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun rate() {
        val url =
            "https://play.google.com/store/apps/details?id=com.spudg.sentient"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

}