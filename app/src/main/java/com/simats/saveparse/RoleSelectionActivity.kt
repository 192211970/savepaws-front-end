package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val btnSavior = findViewById<Button>(R.id.btnSavior)
        val btnRescue = findViewById<Button>(R.id.btnRescue)

        btnSavior.setOnClickListener {
            // Navigate to User Signup
            startActivity(Intent(this, RegisterUserActivity::class.java))
        }

        btnRescue.setOnClickListener {
            // Navigate to Organization Signup
            startActivity(Intent(this, RegisterOrganizationActivity::class.java))
        }
    }
}
