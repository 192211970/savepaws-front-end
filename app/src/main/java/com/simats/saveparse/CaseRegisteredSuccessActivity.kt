package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity

class CaseRegisteredSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        // 1. Link your views (Ensure these IDs match your layout)
        val icon = findViewById<View>(R.id.successIconContainer)
        val title = findViewById<View>(R.id.tvSuccessTitle)
        val message = findViewById<View>(R.id.tvSuccessMessage)

        // 2. Load and start the animation immediately
        val popAnim = AnimationUtils.loadAnimation(this, R.anim.success_flash)

        icon.visibility = View.VISIBLE
        icon.startAnimation(popAnim)

        title.visibility = View.VISIBLE
        title.startAnimation(popAnim)

        message.visibility = View.VISIBLE
        message.startAnimation(popAnim)

        // 3. Redirect to UserDashboard after 3 seconds (Splash effect)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, UserDashboardActivity::class.java))
            finish()
        }, 3000) // 3000ms gives enough time for the animation to play
    }
}