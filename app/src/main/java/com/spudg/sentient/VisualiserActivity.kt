package com.spudg.sentient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spudg.sentient.databinding.ActivityVisualiserBinding

class VisualiserActivity : AppCompatActivity() {

    private lateinit var bindingVisualiser: ActivityVisualiserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingVisualiser = ActivityVisualiserBinding.inflate(layoutInflater)
        val view = bindingVisualiser.root
        setContentView(view)

        bindingVisualiser.backToRecordsFromVisualiser.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }







    }

}