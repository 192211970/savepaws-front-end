package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay 2 seconds → open LoginActivity
        android.os.Handler().postDelayed({
            startActivity(Intent(this, SubscriptionActivity::class.java))
            finish()
        }, 2000)
    }
}
