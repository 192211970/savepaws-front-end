package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DonationMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_menu)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val cardRaiseDonation = findViewById<CardView>(R.id.cardRaiseDonation)
        val cardCheckStatus = findViewById<CardView>(R.id.cardCheckStatus)

        btnBack.setOnClickListener { finish() }

        cardRaiseDonation.setOnClickListener {
            startActivity(Intent(this, RaiseDonationActivity::class.java))
        }

        cardCheckStatus.setOnClickListener {
            startActivity(Intent(this, DonationStatusActivity::class.java))
        }
    }
}
